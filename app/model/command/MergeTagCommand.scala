package model.command

import model.command.CommandError._
import model.jobs.JobHelper
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}
import repositories._
import services.Contexts

import scala.concurrent.Future


case class MergeTagCommand(removingTagId: Long, replacementTagId: Long) extends Command {
  override type T = Unit

  override def process()(implicit username: Option[String] = None): Future[Option[T]] = Future{
    if (removingTagId == replacementTagId) {
      AttemptedSelfMergeTag
    }

    val removingTag = TagRepository.getTag(removingTagId) getOrElse(TagNotFound)
    val replacementTag = TagRepository.getTag(replacementTagId) getOrElse(TagNotFound)

    if (removingTag.`type` != replacementTag.`type`) {
      MergeTagTypesDontMatch
    }

    if (MergeTagCommand.blockedTagTypes.contains(removingTag.`type`)
      || MergeTagCommand.blockedTagTypes.contains(replacementTag.`type`)) {
      IllegalMergeTagType
    }

    JobHelper.beginMergeTag(removingTag, replacementTag)
    Some(())
  }(Contexts.tagOperationContext)
}

object MergeTagCommand {
  val blockedTagTypes = List("Publication", "NewspaperBook", "NewspaperBookSection", "Tracking", "ContentType", "Campaign")

  implicit val mergeTagCommandFormat: Format[MergeTagCommand] = (
    (JsPath \ "removingTagId").format[Long] and
    (JsPath \ "replacementTagId").format[Long]
  )(MergeTagCommand.apply, unlift(MergeTagCommand.unapply))
}
