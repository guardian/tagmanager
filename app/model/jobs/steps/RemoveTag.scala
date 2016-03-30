package model.jobs.steps

import repositories.TagRepository
import scala.concurrent.duration._
import scala.util.control.NonFatal
import model.jobs.{Step, StepStatus}
import model.Tag
import play.api.Logger

case class RemoveTag(tag: Tag, `type`: String = RemoveTag.`type`, var stepStatus: String = StepStatus.ready, var stepMessage: String = "Waiting", var attempts: Int = 0) extends Step {
  override def process = {
    TagRepository.deleteTag(tag.id)
    Logger.info(s"Removing tag ${tag.id} from tag manager")
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

  override val checkingMessage = s"Checking if '${tag.path}' was removed from Tag Manager."
  override val failureMessage = s"Failed to remove tag '${tag.path}' from Tag Managers database."
  override val checkFailMessage = s"Could not verify '${tag.path}' was successfully removed from Tag Manager."
}

object RemoveTag {
  val `type` = "remove-tag"
}
