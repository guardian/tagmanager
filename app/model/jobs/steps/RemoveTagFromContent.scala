package model.jobs.steps

import com.gu.tagmanagement.{TagWithSection, OperationType, TaggingOperation}
import model.{Section, Tag}
import scala.concurrent.duration._
import model.jobs.{Step, StepStatus}
import play.api.Logger
import services.KinesisStreams
import repositories.ContentAPI

case class RemoveTagFromContent(tag: Tag, section: Option[Section] = None, contentIds: List[String],
  `type`: String = RemoveTagFromContent.`type`, var stepStatus: String = StepStatus.ready) extends Step {
  override def process = {
    contentIds foreach { contentPath =>
      val taggingOperation = TaggingOperation(
        operation = OperationType.Remove,
        contentPath = contentPath,
        tag = Some(TagWithSection(tag.asThrift, section.map(_.asThrift)))
      )
      KinesisStreams.taggingOperationsStream.publishUpdate(contentPath.take(200), taggingOperation)
    }
  }

  override def waitDuration: Option[Duration] = {
    Some(5 seconds)
  }

  override def check: Boolean = {
    val count = ContentAPI.countOccurencesOfTagInContents(contentIds, tag.path)
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

  override def failureMessage = s"Failed to remove tag '${tag.id}' from listed content."
}

object RemoveTagFromContent {
  val `type` = "remove-tag-from-content"
}
