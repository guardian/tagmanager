package model.jobs.steps

import com.gu.tagmanagement.OperationType
import repositories.{PathManager, TagRepository}
import scala.concurrent.duration._
import model.Tag
import model.jobs.{Step, StepStatus}
import scala.util.control.NonFatal

case class RemoveTagPath(var tag: Tag, `type`: String = RemoveTagPath.`type`, var stepStatus: String = StepStatus.ready) extends Step {
  override def process = {
      PathManager.removePathForId(tag.pageId)
  }

  override def waitDuration: Option[Duration] = {
    Some(5 seconds)
  }

  override def check: Boolean = {
    PathManager.isPathInUse(tag.path)
  }

  override def rollback = {
    val newId = PathManager.registerPathAndGetPageId(tag.path)
    val newTag = tag.copy(pageId = newId)

    TagRepository.upsertTag(newTag)
  }

  override def failureMessage = s"Failed to remove tag path '${tag.path}' from Path Manager."
}

object RemoveTagPath {
  val `type` = "remove-tag-path"
}
