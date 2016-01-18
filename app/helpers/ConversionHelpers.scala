package helpers

import model.command.Command
import play.api.libs.json._

object ConversionHelpers {

  def commandToType[T](command: Command)(implicit r: Reads[T]): T = {
    val json = Json.toJson(command)
    json.as[T]
  }

}
