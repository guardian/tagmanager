package model

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsValue, Json, JsPath, Format}
import scala.concurrent.{Future}

case class ClientConfig(username: String,
                        capiUrl: String,
                        capiPreviewUrl: String,
                        capiKey: String,
                        tagTypes: List[String],
                        permittedTagTypes: List[String],
                        permissions: Map[String, Boolean],
                        reauthUrl: String)

object ClientConfig {

  implicit val clientConfigFormat: Format[ClientConfig] = (
      (JsPath \ "username").format[String] and
      (JsPath \ "capiUrl").format[String] and
      (JsPath \ "capiPreviewUrl").format[String] and
      (JsPath \ "capiKey").format[String] and
      (JsPath \ "tagTypes").format[List[String]] and
      (JsPath \ "permittedTagTypes").format[List[String]] and
      (JsPath \ "permissions").format[Map[String, Boolean]] and
      (JsPath \ "reauthUrl").format[String]
    )(ClientConfig.apply, unlift(ClientConfig.unapply))
}
