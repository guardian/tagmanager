package model.jobs

import com.gu.pandomainauth.model.User
import com.gu.tagmanagement.{EventType, TagEvent}
import model.TagAudit
import play.api.Logger
import repositories.{ContentAPI, TagAuditRepository, TagRepository}
import services.KinesisStreams


case class AllUsagesOfTagRemovedCheck(apiTagId: String, originalCount: Int, completed: Int = 0) extends Step {
  /** runs the step and returns the updated state of the step, or None if the step has completed */
  override def process: Option[Step] = {
    val currentCount = ContentAPI.countContentWithTag(apiTagId)
    if (currentCount == 0) {
      Logger.info(s"no content remains with tag $apiTagId")
      None
    } else {
      val completedCount = originalCount - currentCount
      Logger.info(s"removing tag $apiTagId from content, $completedCount completed out of $originalCount")
      Some(this.copy(completed = completedCount))
    }
  }
}

case class RemoveTagStep(tagId: Long, originalUsername: Option[String]) extends Step {
  /** runs the step and returns the updated state of the step, or None if the step has completed */
  override def process: Option[Step] = {
    Logger.info(s"deleting tag $tagId}")
    TagRepository.getTag(tagId) foreach { tag =>

      TagRepository.deleteTag(tagId)

      KinesisStreams.tagUpdateStream.publishUpdate(tagId.toString, TagEvent(EventType.Delete, tagId, None))

      TagAuditRepository.upsertTagAudit(TagAudit.deleted(tag, originalUsername))
    }
    None
  }
}

case class TagRemovedCheck(apiTagId: String) extends Step {
  /** runs the step and returns the updated state of the step, or None if the step has completed */
  override def process: Option[Step] = {
    ContentAPI.getTag(apiTagId) match {
      case Some(tag) => {
        Logger.info(s"tag $apiTagId still exists}")
        Some(this)
      }
      case None => {
        Logger.info(s"tag $apiTagId successfully removed")
        None
      }
    }
  }
}
