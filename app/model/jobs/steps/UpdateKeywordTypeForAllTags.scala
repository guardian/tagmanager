package model.jobs.steps

import com.gu.tagmanagement.{EventType, TagEvent}
import model.{KeywordType, TagAudit}
import model.jobs.{Step, StepStatus}
import play.api.Logging
import repositories.{TagAuditRepository, TagLookupCache, TagRepository}
import services.KinesisStreams

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.Try
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
  var totalCount: Int = 0,
  var failedPaths: List[String] = List.empty,
  var skippedPaths: List[String] = List.empty
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
    failedPaths = List.empty
    skippedPaths = List.empty

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
        logger.warn(s"$failedCount tags failed to update. Failed paths: ${failedPaths.mkString(", ")}")
      }

      if (skippedCount > 0) {
        logger.info(s"$skippedCount tags were skipped. Skipped paths: ${skippedPaths.mkString(", ")}")
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
          failedPaths = failedPaths :+ tagPath
      }
      processedCount += 1
    }
  }

  private def skipPath(tagPath: String, message: String): Unit = {
    logger.warn(message)
    skippedCount += 1
    skippedPaths = skippedPaths :+ tagPath
  }

  private def failPath(tagPath: String): Unit = {
    logger.error(s"Failed to upsert tag '$tagPath'")
    failedCount += 1
    failedPaths = failedPaths :+ tagPath
  }

  private def processTag(tagPath: String, keywordTypeStr: String, tagsByPath: Map[String, model.Tag])(implicit username: Option[String]): Unit =
    for {
      tag <- tagsByPath.get(tagPath).toRight().left.map { _ =>
        skipPath(tagPath, s"Tag with path '$tagPath' not found, skipping")
      }
      keywordType <- Try(KeywordType.withName(keywordTypeStr)).toEither.left.map { _ =>
        skipPath(tagPath, s"Invalid keyword type '$keywordTypeStr' for tag '$tagPath', skipping")
      }
      updatedTag = tag.copy(keywordType = Some(keywordType), updatedAt = System.currentTimeMillis())
      result <- TagRepository.upsertTag(updatedTag).toRight().left.map { _ =>
        failPath(tagPath)
      }
    } yield {
      TagLookupCache.insertTag(result)
      KinesisStreams.tagUpdateStream.publishUpdate(
        result.id.toString,
        TagEvent(EventType.Update, result.id, Some(result.asThrift))
      )
      TagAuditRepository.upsertTagAudit(TagAudit.updated(result))
      logger.info(s"Updated keyword type for tag ${result.id} (${result.path}) to ${keywordType.entryName}")
      successCount += 1
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

