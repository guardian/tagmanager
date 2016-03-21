package model.jobs.steps

import com.gu.tagmanagement.{TagWithSection, OperationType, TaggingOperation}
import model.{Section, Tag}
import scala.concurrent.duration._
import model.jobs.{Step, StepStatus}
import play.api.Logger
import services.KinesisStreams
import repositories.ContentAPI

case class RemoveTagFromContent(tag: Tag, section: Option[Section] = None, contentIds: List[String],
  `type`: String = RemoveTagFromContent.`type`, var stepStatus: String = StepStatus.ready, var stepMessage: String = "Waiting", var attempts: Int = 0) extends Step {
  override def process = {
    contentIds foreach { contentPath =>
      val taggingOperation = TaggingOperation(
        operation = OperationType.Remove,
        contentPath = contentPath,
        tag = Some(TagWithSection(tag.asThrift, section.map(_.asThrift)))
      )
      Logger.info(s"raising ${OperationType.Remove} for ${tag.id} on ${contentPath} operation")
      KinesisStreams.taggingOperationsStream.publishUpdate(contentPath.take(200), taggingOperation)
    }
  }

  override def waitDuration: Option[Duration] = {
    Some(5 seconds)
  }

  override def check: Boolean = {
    val count = ContentAPI.countOccurencesOfTagInContents(contentIds, tag.path)
    Logger.info(s"Checking batch tag deletion. Expected=0 Actual=${count}")
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
  }

  override val checkingMessage = s"Checking if '${tag.path}' was removed from all content in CAPI."
  override val failureMessage = s"Failed to remove tag '${tag.path}' from listed content."
  override val checkFailMessage = s"Tag '${tag.path}' was not removed from all pieces of content in CAPI."
}

object RemoveTagFromContent {
  val `type` = "remove-tag-from-content"
}
