package model.jobs.steps

import com.gu.tagmanagement.OperationType
import repositories.{PathManager, TagRepository}
import scala.concurrent.duration._
import model.Tag
import model.jobs.{Step, StepStatus}
import scala.util.control.NonFatal
import play.api.Logger

case class RemoveTagPath(var tag: Tag, `type`: String = RemoveTagPath.`type`, var stepStatus: String = StepStatus.ready, var stepMessage: String = "Waiting", var attempts: Int = 0) extends Step {
  override def process = {
      PathManager.removePathForId(tag.pageId)
      Logger.info(s"Removing path for ${tag.id}")
  }

  override def waitDuration: Option[Duration] = {
    Some(5 seconds)
  }

  override def check: Boolean = {
    // Our PathManager object will throw if path manager returns anything other than a 204
    // so we can assume this went ok - otherwise we'd have a process failure.
    true
  }

  override def rollback = {
    val newId = PathManager.registerPathAndGetPageId(tag.path)
    val newTag = tag.copy(pageId = newId)

    TagRepository.upsertTag(newTag)
  }

  override val checkingMessage = s"Checking if tag path '${tag.path}' was removed from path manager."
  override val failureMessage = s"Failed to remove tag path '${tag.path}' from Path Manager."
  override val checkFailMessage = s"Path '${tag.path}' is still in use."
}

object RemoveTagPath {
  val `type` = "remove-tag-path"
}
