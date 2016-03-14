package model.jobs.steps

import repositories.TagRepository
import scala.concurrent.duration._
import scala.util.control.NonFatal
import model.jobs.{Step, StepStatus}
import model.Tag

case class RemoveTag(tag: Tag, `type`: String = RemoveTag.`type`, var stepStatus: String = StepStatus.ready) extends Step {
  override def process = {
    TagRepository.deleteTag(tag.id)
  }

  override def waitDuration: Option[Duration] = {
    None
  }

  override def check: Boolean = {
    // No check to be done, TagRepository calls are synchronous so `process` will fail if we didn't delete
    true
  }

  override def rollback = {
    TagRepository.upsertTag(tag)
  }

  override def failureMessage = s"Failed to remove tag '${tag.id}' from Tag Managers database."
}

object RemoveTag {
  val `type` = "remove-tag"
}
