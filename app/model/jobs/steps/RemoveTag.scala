package model.jobs.steps

import repositories.TagRepository
import scala.concurrent.duration._
import scala.util.control.NonFatal
import model.jobs.Step
import model.Tag

case class RemoveTag(tag: Tag) extends Step {
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

  override def audit = {

  }

  override def failureMessage = s"Failed to remove tag '${tag.id}' from Tag Managers database."

  override val `type` = RemoveTag.`type`
}

object RemoveTag {
  val `type` = "remove-tag"
}
