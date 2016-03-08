package model.command

import com.gu.tagmanagement.{TagWithSection, OperationType, TaggingOperation}
import model.command.CommandError._
import model.jobs.JobHelper
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Format}
import repositories._
import services.{SQS, KinesisStreams}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


case class MergeTagCommand(removingTagId: Long, replacementTagId: Long) extends Command {
  override type T = Unit

  override def process()(implicit username: Option[String] = None): Option[T] = {
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
  }
}

object MergeTagCommand {
  val blockedTagTypes = List("Publication", "NewspaperBook", "NewspaperBookSection", "Tracking", "ContentType", "Contributor")

  implicit val mergeTagCommandFormat: Format[MergeTagCommand] = (
    (JsPath \ "removingTagId").format[Long] and
    (JsPath \ "replacementTagId").format[Long]
  )(MergeTagCommand.apply, unlift(MergeTagCommand.unapply))
}
