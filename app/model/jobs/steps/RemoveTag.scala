package model.jobs.steps

import repositories.{TagRepository, TagAuditRepository}
import scala.concurrent.duration._
import scala.util.control.NonFatal
import model.jobs.{Step, StepStatus}
import model.{Tag, TagAudit}
import play.api.Logging

case class RemoveTag(
  tag: Tag,
  username: Option[String],
  `type`: String = RemoveTag.`type`,
  var stepStatus: String = StepStatus.ready,
  var stepMessage: String = "Waiting",
  var attempts: Int = 0
) extends Step
  with Logging {

  override def process = {
    TagRepository.deleteTag(tag.id)
    logger.info(s"Removing tag ${tag.id} from tag manager")
    TagAuditRepository.upsertTagAudit(TagAudit.deleted(tag, username))
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

    implicit val uname = username
    TagAuditRepository.upsertTagAudit(TagAudit.created(tag))
  }

  override val checkingMessage = s"Checking if '${tag.path}' was removed from Tag Manager."
  override val failureMessage = s"Failed to remove tag '${tag.path}' from Tag Managers database."
  override val checkFailMessage = s"Could not verify '${tag.path}' was successfully removed from Tag Manager."
}

object RemoveTag {
  val `type` = "remove-tag"
}
