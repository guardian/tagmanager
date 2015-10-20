package model

import play.api.libs.json._
import play.api.libs.functional.syntax._



case class Reference(`type`: String, value: String)

object Reference {

  implicit val referenceFormat: Format[Reference] = (
      (JsPath \ "type").format[String] and
      (JsPath \ "value").format[String]
    )(Reference.apply, unlift(Reference.unapply))
}
