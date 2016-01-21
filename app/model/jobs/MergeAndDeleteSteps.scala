package model.jobs

import com.gu.pandomainauth.model.User
import com.gu.tagmanagement.{EventType, TagEvent}
import model.TagAudit
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Format}
import repositories.{PathManager, ContentAPI, TagAuditRepository, TagRepository}
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

object AllUsagesOfTagRemovedCheck {
  implicit val allUsagesOfTagRemovedCheckFormat: Format[AllUsagesOfTagRemovedCheck] = (
      (JsPath \ "apiTagId").format[String] and
      (JsPath \ "originalCount").format[Int] and
      (JsPath \ "completed").format[Int]
    )(AllUsagesOfTagRemovedCheck.apply, unlift(AllUsagesOfTagRemovedCheck.unapply))
}

case class RemoveTagStep(tagId: Long, originalUsername: Option[String]) extends Step {
  /** runs the step and returns the updated state of the step, or None if the step has completed */
  override def process: Option[Step] = {
    Logger.info(s"deleting tag $tagId}")
    TagRepository.getTag(tagId) foreach { tag =>

      TagRepository.deleteTag(tagId)
      PathManager.removePathForId(tag.pageId)

      KinesisStreams.tagUpdateStream.publishUpdate(tagId.toString, TagEvent(EventType.Delete, tagId, None))

      TagAuditRepository.upsertTagAudit(TagAudit.deleted(tag, originalUsername))
    }
    None
  }
}

object RemoveTagStep {
  implicit val removeTagStepFormat: Format[RemoveTagStep] = (
    (JsPath \ "tagId").format[Long] and
      (JsPath \ "originalUsername").formatNullable[String]
    )(RemoveTagStep.apply, unlift(RemoveTagStep.unapply))
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

case class MergeAuditStep(
  removingTagId: Long,
  replacementTagId: Long,
  username: Option[String]) extends Step {
  override def process: Option[Step] = {
    Logger.info(s"replacing: ${removingTagId} with ${replacementTagId}")

    val removingTag = TagRepository.getTag(removingTagId)
    val replacementTag = TagRepository.getTag(replacementTagId)

    (removingTag, replacementTag) match {
      case (Some(removing), Some(replacing)) => {
        val audit = TagAudit.merged(removing, replacing, username)
        TagAuditRepository.upsertTagAudit(audit)
      }
      case (_, _ ) => None
    }
    None
  }
}

object MergeAuditStep {
  implicit val mergeAuditStepFormat: Format[MergeAuditStep] = (
    (JsPath \ "removingTagId").format[Long] and
    (JsPath \ "replacementTagId").format[Long] and
      (JsPath \ "username").formatNullable[String]
    )(MergeAuditStep.apply, unlift(MergeAuditStep.unapply))
}
