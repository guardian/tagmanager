package model.command

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Format}
import repositories.TagRepository
import CommandError._

case class BatchTagCommand(contentIds: List[String], tagId: Long, operation: String) extends Command {
  type T = String

  override def process: Option[String] = {
    val tag = TagRepository.getTag(tagId) getOrElse(TagNotFound)

    contentIds foreach { cid =>

    }

    Some("")
  }
}

object BatchTagCommand {
  implicit val batchTagCommandFormat: Format[BatchTagCommand] = (
      (JsPath \ "contentIds").formatNullable[List[String]].inmap[List[String]](_.getOrElse(Nil), Some(_)) and
      (JsPath \ "tagId").format[Long] and
      (JsPath \ "operation").format[String]
    )(BatchTagCommand.apply, unlift(BatchTagCommand.unapply))
}
