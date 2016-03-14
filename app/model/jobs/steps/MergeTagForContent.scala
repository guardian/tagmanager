package model.jobs.steps

import com.gu.tagmanagement.{TagWithSection, OperationType, TaggingOperation}
import model.{Section, Tag}
import scala.concurrent.duration._
import model.jobs.{Step, StepStatus}
import play.api.Logger
import services.KinesisStreams
import repositories._
import java.lang.UnsupportedOperationException

case class MergeTagForContent(from: Tag, to: Tag, fromSection: Option[Section], toSection: Option[Section], contentIds: List[String],
  `type`: String = MergeTagForContent.`type`, var stepStatus: String = StepStatus.ready) extends Step {

  override def process = {
    contentIds foreach { contentPath =>
      val taggingOperation = TaggingOperation(
        operation = OperationType.Merge,
        contentPath = contentPath,
        tag = Some(TagWithSection(from.asThrift, fromSection.map(_.asThrift))),
        destinationTag = Some(TagWithSection(to.asThrift, toSection.map(_.asThrift)))
      )
      Logger.info(s"raising merge tag ${from.path} -> ${to.path} for content $contentPath")
      KinesisStreams.taggingOperationsStream.publishUpdate(contentPath.take(200), taggingOperation)
    }
  }

  override def waitDuration: Option[Duration] = {
    Some(5 seconds)
  }

  override def check: Boolean = {
    val removedCount = ContentAPI.countContentWithTag(from.path)
    val addedCount = ContentAPI.countContentWithTag(to.path)

    if (removedCount == 0 && addedCount == contentIds.size) {
      true
    } else {
      false
    }
  }

  override def rollback = {
    throw new UnsupportedOperationException("Rollback is not supported for merging tags in content.")
  }

  override def failureMessage = s"Failed to merge tag '${from.id}' to '${to.id}' all content."
}

object MergeTagForContent {
  val `type` = "merge-tag-for-content"
}
