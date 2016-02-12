package model

import org.cvogt.play.json.Jsonx
import play.api.libs.functional.syntax._
import play.api.libs.json.Json
import scala.concurrent.{Future}

case class ClientConfig(capiUrl: String,
                        capiPreviewUrl: String,
                        capiKey: String,
                        tagTypes: List[String],
                        permittedTagTypes: List[String],
                        permissions: Map[String, Boolean],
                        reauthUrl: String)

object ClientConfig {

  implicit val clientConfigFormat = Jsonx.formatCaseClass[ClientConfig]
}
