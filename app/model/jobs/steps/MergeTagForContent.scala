package model.jobs.steps

import com.gu.tagmanagement.{TagWithSection, OperationType, TaggingOperation}
import model.{Section, Tag}
import scala.concurrent.duration._
import model.jobs.{Step, StepStatus}
import play.api.Logger
import services.KinesisStreams
import repositories._
import java.lang.UnsupportedOperationException

case class MergeTagForContent(from: Tag, to: Tag, fromSection: Option[Section], toSection: Option[Section], var contentCount: Int = -1,
  `type`: String = MergeTagForContent.`type`, var stepStatus: String = StepStatus.ready, var stepMessage: String = "Waiting", var attempts: Int = 0) extends Step {

  override def process = {
    val contentIds = ContentAPI.getContentIdsForTag(from.path)
    contentCount = contentIds.size + ContentAPI.countContentWithTag(to.path)

    contentIds foreach { contentPath =>
      val taggingOperation = TaggingOperation(
        operation = OperationType.Merge,
        contentPath = contentPath,
        tag = Some(TagWithSection(from.asThrift, fromSection.map(_.asThrift))),
        destinationTag = Some(TagWithSection(to.asThrift, toSection.map(_.asThrift)))
      )
      KinesisStreams.taggingOperationsStream.publishUpdate(contentPath.take(200), taggingOperation)
      Logger.info(s"raising merge tag ${from.path} -> ${to.path} for content $contentPath")
    }
  }

  override def waitDuration: Option[Duration] = {
    Some(5 seconds)
  }

  override def check: Boolean = {
    val fromCount = ContentAPI.countContentWithTag(from.path)
    val toCount = ContentAPI.countContentWithTag(to.path)

    Logger.info(s"Checking merge tag CAPI counts. From tag: '${from.path}' remains on ${fromCount} pieces of content. To tag: '${to.path}' on to ${toCount} pieces of content, ${contentCount - toCount} left to add.")
    if (fromCount == 0 && toCount == contentCount) {
      true
    } else {
      false
    }
  }

  override def rollback = {
    throw new UnsupportedOperationException("Rollback is not supported for merging tags in content.")
  }

  override val checkingMessage = s"Checking if '${from.path}' is merged into '${to.path}' for all content in CAPI."
  override val failureMessage = s"Failed to merge tag '${from.path}' to '${to.path}' all content."
  override val checkFailMessage = s"CAPI still contains content where tag '${from.path}' has not been merged into '${to.path}'."
}

object MergeTagForContent {
  val `type` = "merge-tag-for-content"
}
