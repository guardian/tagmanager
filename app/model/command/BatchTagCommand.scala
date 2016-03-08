package model.command

import com.gu.tagmanagement.{TagWithSection, OperationType, TaggingOperation}
import model.TagAudit
import model.jobs.JobHelper
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Format}
import repositories._
import CommandError._
import services.{SQS, KinesisStreams}

case class BatchTagCommand(contentIds: List[String], tagId: Long, operation: String) extends Command {
  type T = Unit

  override def process()(implicit username: Option[String] = None): Option[Unit] = {
    val tag = TagRepository.getTag(tagId) getOrElse(TagNotFound)

    operation match {
      case "remove" => { JobHelper.beginBatchTagDeletion(tag, operation, contentIds) }
      case _ => { JobHelper.beginBatchTagAddition(tag, operation, contentIds) }
    }

    Some(())
  }
}

object BatchTagCommand {
  implicit val batchTagCommandFormat: Format[BatchTagCommand] = (
      (JsPath \ "contentIds").formatNullable[List[String]].inmap[List[String]](_.getOrElse(Nil), Some(_)) and
      (JsPath \ "tagId").format[Long] and
      (JsPath \ "operation").format[String]
    )(BatchTagCommand.apply, unlift(BatchTagCommand.unapply))
}
