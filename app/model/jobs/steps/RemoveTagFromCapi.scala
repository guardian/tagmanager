package model.jobs.steps

import com.gu.tagmanagement.{EventType, TagEvent}
import model.Tag
import repositories.ContentAPI
import services.KinesisStreams

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import model.jobs.{Step, StepStatus}
import play.api.Logging

import scala.language.postfixOps

case class RemoveTagFromCapi(tag: Tag,
  `type`: String = RemoveTagFromCapi.`type`, var stepStatus: String = StepStatus.ready, var stepMessage: String = "Waiting", var attempts: Int = 0) extends Step {
  override def process(implicit ec: ExecutionContext) = {
      KinesisStreams.tagUpdateStream.publishUpdate(
        tag.id.toString,
        TagEvent(EventType.Delete, tag.id, Some(tag.asThrift)))
      logger.info(s"Removing tag ${tag.id} from CAPI")
  }

  override def waitDuration: Option[Duration] = {
    Some(5 seconds)
  }

  override def check(implicit ec: ExecutionContext): Boolean = {
    ContentAPI.getTag(tag.path) match {
      case Some(_) => false
      case None => true
    }
  }

  override def rollback = {
      KinesisStreams.tagUpdateStream.publishUpdate(
        tag.id.toString,
        TagEvent(EventType.Update, tag.id, Some(tag.asThrift)))
  }

  override val checkingMessage = s"Checking if '${tag.path}' was removed from CAPI."
  override val failureMessage = s"Failed to remove tag '${tag.path}' from CAPI."
  override val checkFailMessage = s"CAPI did not remove '${tag.path}'."
}

object RemoveTagFromCapi {
  val `type` = "remove-tag-from-capi"
}
