package model.jobs.steps

import com.gu.tagmanagement.{OperationType, TagWithSection, TaggingOperation}
import model.{Section, Tag, TagAudit}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import model.jobs.{Step, StepStatus}
import play.api.Logging
import services.KinesisStreams
import repositories.{ContentAPI, TagAuditRepository}

import scala.language.postfixOps

case class RemoveTagFromContent(
  tag: Tag,
  section: Option[Section],
  contentIds: List[String],
  `type`: String = RemoveTagFromContent.`type`,
  var stepStatus: String = StepStatus.ready,
  var stepMessage: String = "Waiting",
  var attempts: Int = 0
) extends Step
  with Logging {

  override def process(implicit ec: ExecutionContext) = {
    contentIds foreach { contentPath =>
      val taggingOperation = TaggingOperation(
        operation = OperationType.Remove,
        contentPath = contentPath,
        tag = Some(TagWithSection(tag.asThrift, section.map(_.asThrift)))
      )
      KinesisStreams.taggingOperationsStream.publishUpdate(contentPath.take(128), taggingOperation)
      logger.info(s"raising ${OperationType.Remove} for ${tag.id} on $contentPath operation")
    }
    TagAuditRepository.upsertTagAudit(TagAudit.batchTag(tag, "remove", contentIds.length))
  }

  override def waitDuration: Option[Duration] = {
    Some(5 seconds)
  }

  override def check(implicit ec: ExecutionContext): Boolean = {
    val count = ContentAPI.countOccurencesOfTagInContents(contentIds, tag.path)
    logger.info(s"Checking tags removed from content. Expected=0 Actual=$count")
    if (count == 0) {
      true
    } else {
      false
    }
  }

  override def rollback = {
    contentIds foreach { contentPath =>
      val taggingOperation = TaggingOperation(
        operation = OperationType.AddToBottom,
        contentPath = contentPath,
        tag = Some(TagWithSection(tag.asThrift, section.map(_.asThrift)))
      )
      KinesisStreams.taggingOperationsStream.publishUpdate(contentPath.take(200), taggingOperation)
    }
    implicit val username: Option[String] = None // unfortunately we don't have easy access to job.createdBy here
    TagAuditRepository.upsertTagAudit(TagAudit.batchTag(tag, OperationType.AddToBottom.toString, contentIds.length))
  }

  override val checkingMessage = s"Checking if '${tag.path}' was removed from content in CAPI."
  override val failureMessage = s"Failed to remove tag '${tag.path}' from content."
  override val checkFailMessage = s"Tag '${tag.path}' was not removed from all selected content in CAPI."
}

object RemoveTagFromContent {
  val `type` = "remove-tag-from-content"
}
