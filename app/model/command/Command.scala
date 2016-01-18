package model.command

import play.api.Logger
import play.api.libs.json._
import play.api.libs.functional.syntax._

trait Command {
  type T

  def process()(implicit username: Option[String] = None): Option[T]

}

// Provides polymorphic json format for commands, note not all commands need to be supported here, just those
// that are polymophically serialised as part of jobs.
object Command {
  val commandWrites = new Writes[Command] {
    override def writes(c: Command): JsValue = c match {
      case b: BatchTagCommand => BatchTagCommand.batchTagCommandFormat.writes(b).asInstanceOf[JsObject] + ("type", JsString("BatchTagCommand"))
      case m: MergeTagCommand => MergeTagCommand.mergeTagCommandFormat.writes(m).asInstanceOf[JsObject] + ("type", JsString("MergeTagCommand"))
      case d: DeleteTagCommand => JsObject(Map("type" -> JsString("DeleteTagCommand"), "removingTagId" -> JsNumber(d.removingTagId)))
      case r: ReindexCommand => ReindexCommand.reindexCommandFormat.writes(r).asInstanceOf[JsObject] + ("type", JsString("ReindexCommand"))
      case other => {
        Logger.warn(s"unable to serialise command of type ${other.getClass}")
        throw new UnsupportedOperationException(s"unable to serialise command of type ${other.getClass}")
      }
    }
  }

  val commandReads = new Reads[Command] {
    override def reads(json: JsValue): JsResult[Command] = {
      (json \ "type").get match {
        case JsString("BatchTagCommand") => BatchTagCommand.batchTagCommandFormat.reads(json)
        case JsString("MergeTagCommand") => MergeTagCommand.mergeTagCommandFormat.reads(json)
        case JsString("DeleteTagCommand") => (json \ "removingTagId").validate[Long].map(DeleteTagCommand)
        case JsString("ReindexCommand") => ReindexCommand.reindexCommandFormat.reads(json)
        case JsString(other) => JsError(s"unsupported command type $other}")
        case _ => JsError(s"unexpected command type value")
      }
    }
  }

  implicit val commandFormat: Format[Command] = Format(commandReads, commandWrites)
}
