package model.jobs.steps

import com.gu.tagmanagement.{TagWithSection, OperationType, TaggingOperation}
import model.{Section, Tag}
import scala.concurrent.duration._
import model.jobs.Step
import play.api.Logger
import services.KinesisStreams

case class AddTagToContent(tag: Tag, section: Option[Section], contentIds: List[String], top: Boolean) extends Step {
  override def process = {
    val op =  if (top) OperationType.AddToTop else OperationType.AddToBottom

    contentIds foreach { contentPath =>
      val taggingOperation = TaggingOperation(
        operation = op,
        contentPath = contentPath,
        tag = Some(TagWithSection(tag.asThrift, section.map(_.asThrift)))
      )
      Logger.info(s"raising add tag ${tag.path} for content $contentPath")
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

  override def failureMessage = s"Failed to add tag '${tag.id}' to listed content."

  override val `type` = AddTagToContent.`type`
}

object AddTagToContent {
  val `type` = "add-tag-to-content"
}
