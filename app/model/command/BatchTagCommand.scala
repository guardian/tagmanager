package model.command

import model.command.CommandError._
import model.jobs.JobHelper
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}
import repositories._
import services.Contexts

import scala.concurrent.Future

case class BatchTagCommand(contentIds: List[String], tagId: Long, operation: String) extends Command {
  type T = Unit

  override def process()(implicit username: Option[String] = None): Future[Option[Unit]] = Future{
    val tag = TagRepository.getTag(tagId) getOrElse(TagNotFound)

    operation match {
      case "remove" => { JobHelper.beginBatchTagDeletion(tag, operation, contentIds) }
      case _ => { JobHelper.beginBatchTagAddition(tag, operation, contentIds) }
    }

    Some(())
  }(Contexts.tagOperationContext)
}

object BatchTagCommand {
  implicit val batchTagCommandFormat: Format[BatchTagCommand] = (
      (JsPath \ "contentIds").formatNullable[List[String]].inmap[List[String]](_.getOrElse(Nil), Some(_)) and
      (JsPath \ "tagId").format[Long] and
      (JsPath \ "operation").format[String]
    )(BatchTagCommand.apply, unlift(BatchTagCommand.unapply))
}
