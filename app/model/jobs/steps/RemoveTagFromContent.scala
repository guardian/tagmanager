package model.jobs.steps

import com.gu.tagmanagement.{TagWithSection, OperationType, TaggingOperation}
import model.{Section, Tag}
import scala.concurrent.duration._
import model.jobs.{Step, StepStatus}
import play.api.Logger
import services.KinesisStreams

case class RemoveTagFromContent(tag: Tag, section: Option[Section], contentIds: List[String]) extends Step {
  override def process = {
    contentIds foreach { contentPath =>
      val taggingOperation = TaggingOperation(
        operation = OperationType.Remove,
        contentPath = contentPath,
        tag = Some(TagWithSection(tag.asThrift, section.map(_.asThrift)))
      )
      Logger.info(s"raising delete tag ${tag.path} for content $contentPath")
      KinesisStreams.taggingOperationsStream.publishUpdate(contentPath.take(200), taggingOperation)
    }
  }

  override def waitDuration: Option[Duration] = {
    Some(5 seconds)
  }

  override def check: Boolean = {
    false
  }

  override def rollback = {
  }

  override def audit = {

  }

  override def failureMessage = s"Failed to remove tag '${tag.id}' from listed content."

  override val `type` = RemoveTagFromContent.`type`
}

object RemoveTagFromContent {
  val `type` = "remove-tag-from-content"
}
