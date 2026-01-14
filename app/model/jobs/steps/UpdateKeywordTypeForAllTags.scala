package model.jobs.steps

import com.gu.tagmanagement.{EventType, TagEvent}
import model.{KeywordType, TagAudit}
import model.jobs.{Step, StepStatus}
import play.api.Logging
import repositories.{TagAuditRepository, TagLookupCache, TagRepository}
import services.KinesisStreams

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.control.NonFatal

case class UpdateKeywordTypeForAllTags(
  keywordTypeMappings: Map[String, String], // tag path -> keywordType string
  username: Option[String] = None,
  `type`: String = UpdateKeywordTypeForAllTags.`type`,
  var stepStatus: String = StepStatus.ready,
  var stepMessage: String = "Waiting",
  var attempts: Int = 0,
  var processedCount: Int = 0,
  var successCount: Int = 0,
  var skippedCount: Int = 0,
  var failedCount: Int = 0,
  var totalCount: Int = 0
) extends Step with Logging {

  private val BatchSize = 100
  private val BatchDelayMs = 500

  override def process(implicit ec: ExecutionContext): Unit = {
    implicit val un: Option[String] = username
    totalCount = keywordTypeMappings.size
    processedCount = 0
    successCount = 0
    skippedCount = 0
    failedCount = 0

    logger.info(s"Starting keyword type update for $totalCount tags (batch size: $BatchSize, delay: ${BatchDelayMs}ms)")

    // Build a path -> tag lookup map once for efficiency
    val allTags = Option(TagLookupCache.allTags.get()).getOrElse {
      throw new IllegalStateException("Tag cache not initialized - cannot proceed with keyword type update")
    }
    val tagsByPath: Map[String, model.Tag] = allTags.map(t => t.path -> t).toMap
    logger.info(s"Loaded ${tagsByPath.size} tags from cache for lookup")

    try {
      // Process in batches to avoid overwhelming the system
      keywordTypeMappings.grouped(BatchSize).foreach { batch =>
        processBatch(batch, tagsByPath)

        logger.info(s"Progress: $processedCount/$totalCount (success: $successCount, skipped: $skippedCount, failed: $failedCount)")

        // Delay between batches to respect Kinesis rate limits
        if (processedCount < totalCount) {
          Thread.sleep(BatchDelayMs)
        }
      }

      logger.info(s"Completed keyword type update. Total: $totalCount, Success: $successCount, Skipped: $skippedCount, Failed: $failedCount")

      if (failedCount > 0) {
        logger.warn(s"$failedCount tags failed to update - check logs for details")
      }
    } catch {
      case NonFatal(e) =>
        logger.error(s"Keyword type update failed at $processedCount/$totalCount", e)
        throw e
    }
  }

  private def processBatch(batch: Map[String, String], tagsByPath: Map[String, model.Tag])(implicit username: Option[String]): Unit = {
    batch.foreach { case (tagPath, keywordTypeStr) =>
      try {
        processTag(tagPath, keywordTypeStr, tagsByPath)
      } catch {
        case NonFatal(e) =>
          logger.error(s"Unexpected error processing tag '$tagPath': ${e.getMessage}", e)
          failedCount += 1
      }
      processedCount += 1
    }
  }

  private def processTag(tagPath: String, keywordTypeStr: String, tagsByPath: Map[String, model.Tag])(implicit username: Option[String]): Unit = {
    tagsByPath.get(tagPath) match {
      case Some(tag) =>
        val keywordType = try {
          Some(KeywordType.withName(keywordTypeStr))
        } catch {
          case _: NoSuchElementException =>
            logger.warn(s"Invalid keyword type '$keywordTypeStr' for tag '$tagPath', skipping")
            None
        }

        keywordType match {
          case Some(kt) =>
            val updatedTag = tag.copy(keywordType = Some(kt))
            updatedTag.updatedAt = System.currentTimeMillis()

            val result = TagRepository.upsertTag(updatedTag)
            result match {
              case Some(saved) =>
                TagLookupCache.insertTag(saved)
                KinesisStreams.tagUpdateStream.publishUpdate(
                  saved.id.toString,
                  TagEvent(EventType.Update, saved.id, Some(saved.asThrift))
                )
                TagAuditRepository.upsertTagAudit(TagAudit.updated(saved))
                logger.debug(s"Updated keyword type for tag ${saved.id} (${saved.path}) to ${kt.entryName}")
                successCount += 1
              case None =>
                logger.error(s"Failed to upsert tag '$tagPath'")
                failedCount += 1
            }
          case None =>
            skippedCount += 1
        }

      case None =>
        logger.warn(s"Tag with path '$tagPath' not found, skipping")
        skippedCount += 1
    }
  }

  override def waitDuration: Option[Duration] = None

  override def check(implicit ec: ExecutionContext): Boolean = true

  override def rollback: Unit = {
    throw new UnsupportedOperationException("Rollback is not supported for keyword type updates.")
  }

  override val checkingMessage: String = s"Checking keyword type update was successful"
  override val failureMessage: String = s"Failed to update keyword types for tags."
  override val checkFailMessage: String = s"Failed to confirm keyword type update was successful."
}

object UpdateKeywordTypeForAllTags {
  val `type` = "update-keyword-type-for-all-tags"

  def fromCsv(csvContent: String): UpdateKeywordTypeForAllTags = {
    val lines = csvContent.linesIterator.drop(1) // Skip header row, use iterator for efficiency

    val mappings = lines.flatMap { line =>
      val trimmedLine = line.trim
      if (trimmedLine.isEmpty) {
        None
      } else {
        val parts = trimmedLine.split(",").map(_.trim)
        if (parts.length >= 4) {
          val tagPath = parts(0)      // id column
          val keywordType = parts(3)  // keywordType column
          if (tagPath.nonEmpty && keywordType.nonEmpty) {
            Some(tagPath -> keywordType)
          } else {
            None
          }
        } else {
          None
        }
      }
    }.toMap

    UpdateKeywordTypeForAllTags(keywordTypeMappings = mappings)
  }
}

