package model.jobs.steps

import com.gu.tagmanagement.{EventType, TagEvent}
import model.{KeywordType, TagAudit}
import model.jobs.{Step, StepStatus}
import play.api.Logging
import repositories.{TagAuditRepository, TagLookupCache, TagRepository}
import services.KinesisStreams

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

case class UpdateKeywordTypeForAllTags(
  keywordTypeMappings: Map[String, String], // tag path -> keywordType string
  username: Option[String] = None,
  `type`: String = UpdateKeywordTypeForAllTags.`type`,
  var stepStatus: String = StepStatus.ready,
  var stepMessage: String = "Waiting",
  var attempts: Int = 0,
  var processedCount: Int = 0,
  var totalCount: Int = 0
) extends Step with Logging {

  override def process(implicit ec: ExecutionContext): Unit = {
    implicit val un: Option[String] = username
    totalCount = keywordTypeMappings.size
    processedCount = 0

    logger.info(s"Starting keyword type update for $totalCount tags")

    // Build a path -> tag lookup map once for efficiency
    val tagsByPath: Map[String, model.Tag] = TagLookupCache.allTags.get().map(t => t.path -> t).toMap

    keywordTypeMappings.foreach { case (tagPath, keywordTypeStr) =>
      tagsByPath.get(tagPath) match {
        case Some(tag) =>
          val keywordType = try {
            Some(KeywordType.withName(keywordTypeStr))
          } catch {
            case _: NoSuchElementException =>
              logger.warn(s"Invalid keyword type '$keywordTypeStr' for tag '$tagPath', skipping")
              None
          }

          keywordType.foreach { kt =>
            val updatedTag = tag.copy(keywordType = Some(kt))
            updatedTag.updatedAt = System.currentTimeMillis()

            TagRepository.upsertTag(updatedTag).foreach { saved =>
              TagLookupCache.insertTag(saved)
              KinesisStreams.tagUpdateStream.publishUpdate(
                saved.id.toString,
                TagEvent(EventType.Update, saved.id, Some(saved.asThrift))
              )
              TagAuditRepository.upsertTagAudit(TagAudit.updated(saved))
              logger.info(s"Updated keyword type for tag ${saved.id} (${saved.path}) to ${kt.entryName}")
            }
          }

        case None =>
          logger.warn(s"Tag with path '$tagPath' not found, skipping")
      }

      processedCount += 1
      if (processedCount % 100 == 0) {
        logger.info(s"Processed $processedCount / $totalCount tags")
        Thread.sleep(100) // Small delay to avoid overwhelming the system
      }
    }

    logger.info(s"Completed keyword type update for $processedCount tags")
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
    val mappings = csvContent
      .split("\n")
      .drop(1) // Skip header row
      .flatMap { line =>
        val parts = line.trim.split(",").map(_.trim)
        if (parts.length >= 2) {
          val tagPath = parts(0)
          val keywordType = parts(1)
          if (tagPath.nonEmpty && keywordType.nonEmpty) {
            Some(tagPath -> keywordType)
          } else {
            None
          }
        } else {
          None
        }
      }
      .toMap

    UpdateKeywordTypeForAllTags(keywordTypeMappings = mappings)
  }
}

