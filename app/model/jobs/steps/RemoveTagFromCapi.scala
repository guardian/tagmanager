package model.jobs.steps

import com.gu.tagmanagement.{TagEvent, EventType}
import model.Tag
import repositories.ContentAPI
import services.KinesisStreams
import scala.concurrent.duration._
import model.jobs.{Step, StepStatus}

case class RemoveTagFromCapi(tag: Tag,
  `type`: String = RemoveTagFromCapi.`type`, var stepStatus: String = StepStatus.ready) extends Step {
  override def process = {
      KinesisStreams.tagUpdateStream.publishUpdate(
        tag.id.toString,
        TagEvent(EventType.Delete, tag.id, Some(tag.asThrift)))
  }

  override def waitDuration: Option[Duration] = {
    Some(5 seconds)
  }

  override def check: Boolean = {
    ContentAPI.getTag(tag.path) match {
      case Some(tag) => {
        false
      }
      case None => {
        true
      }
    }
  }

  override def rollback = {
      KinesisStreams.tagUpdateStream.publishUpdate(
        tag.id.toString,
        TagEvent(EventType.Update, tag.id, Some(tag.asThrift)))
  }

  override def failureMessage = s"Failed to remove tag '${tag.id}' from CAPI."
}

object RemoveTagFromCapi {
  val `type` = "remove-tag-from-capi"
}
