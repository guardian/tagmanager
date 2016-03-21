package model.jobs.steps

import com.gu.tagmanagement.{TagWithSection, OperationType, TaggingOperation}
import model.{Section, Tag}
import model.jobs.StepStatus
import scala.concurrent.duration._
import model.jobs.Step
import play.api.Logger
import services.KinesisStreams
import repositories.ContentAPI

case class AddTagToContent(tag: Tag, section: Option[Section] = None, contentIds: List[String], op: String, `type`: String = AddTagToContent.`type`, var stepStatus: String = StepStatus.ready, var stepMessage: String = "Waiting", var attempts: Int = 0) extends Step {
  override def process = {
    contentIds foreach { contentPath =>
      val taggingOperation = TaggingOperation(
        operation = OperationType.valueOf(op).get,
        contentPath = contentPath,
        tag = Some(TagWithSection(tag.asThrift, section.map(_.asThrift)))
      )
      Logger.info(s"raising ${op} for ${tag.id} on ${contentPath} operation")
      KinesisStreams.taggingOperationsStream.publishUpdate(contentPath.take(200), taggingOperation)
    }
  }

  override def waitDuration: Option[Duration] = {
    Some(5 seconds)
  }

  override def check: Boolean = {
    val count = ContentAPI.countOccurencesOfTagInContents(contentIds, tag.path)
    Logger.info(s"Checking batch tag addition. Expected=${contentIds.size} Actual=${count}")
    if (count == contentIds.size) {
      true
    } else {
      false
    }
  }

  override def rollback = {
    contentIds foreach { contentPath =>
      val taggingOperation = TaggingOperation(
        operation = OperationType.Remove,
        contentPath = contentPath,
        tag = Some(TagWithSection(tag.asThrift, section.map(_.asThrift)))
      )
      KinesisStreams.taggingOperationsStream.publishUpdate(contentPath.take(200), taggingOperation)
    }
  }

  override val checkingMessage = s"Checking if '${tag.path}' has been added to requested content in CAPI."
  override val failureMessage = s"Failed to issue the tagging operation for adding '${tag.path}' to content."
  override val checkFailMessage = s"Tag '${tag.path}' was not detected on all content in CAPI."
}

object AddTagToContent {
  val `type` = "add-tag-to-content"
}
