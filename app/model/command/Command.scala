package model.command

import com.gu.pandomainauth.model.User
import play.api.Logger
import play.api.libs.json._
import play.api.libs.functional.syntax._

trait Command {
  type T

  def process()(implicit user: Option[User] = None): Option[T]

}

// Provides polymorphic json format for commands, note not all commands need to be supported here, just those
// that are polymophically serialised as part of jobs.
object Command {
  val commandWrites = new Writes[Command] {
    override def writes(c: Command): JsValue = c match {
      case b: BatchTagCommand => BatchTagCommand.batchTagCommandFormat.writes(b).asInstanceOf[JsObject] + ("type", JsString("BatchTagCommand"))
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
        case JsString(other) => JsError(s"unsupported command type $other}")
        case _ => JsError(s"unexpected command type value")
      }
    }
  }

  implicit val commandFormat: Format[Command] = Format(commandReads, commandWrites)
}
