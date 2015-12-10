package model

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsValue, Json, JsPath, Format}

case class ClientConfig(capiUrl: String, capiKey: String, tagTypes: List[String])

object ClientConfig {

  implicit val clientConfigFormat: Format[ClientConfig] = (
    (JsPath \ "capiUrl").format[String] and
      (JsPath \ "capiKey").format[String] and
      (JsPath \ "tagTypes").format[List[String]]
    )(ClientConfig.apply, unlift(ClientConfig.unapply))
}
