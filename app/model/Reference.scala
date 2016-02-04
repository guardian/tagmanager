package model

import play.api.libs.json._
import play.api.libs.functional.syntax._
import com.gu.tagmanagement.{Reference => ThriftReference}
import helpers.XmlHelpers._

case class Reference(`type`: String, value: String, capiType: Option[String]) {
  def asThrift = ThriftReference(`type`, value, capiType)


  def asExportedXml = {
    val el = createElem("external-reference")
    val `type` = createAttribute("type", Some(this.`type`))

    // these are identical but it's what inCopy wants
    val topic = createAttribute("topic", Some(this.value))
    val display = createAttribute("display-name", Some(this.value))

    el % `type` % topic % display
  }
}

object Reference {

  implicit val referenceFormat: Format[Reference] = (
      (JsPath \ "type").format[String] and
      (JsPath \ "value").format[String] and
        (JsPath \ "capiType").formatNullable[String]

    )(Reference.apply, unlift(Reference.unapply))

  def apply(thriftReference: ThriftReference): Reference = Reference(thriftReference.`type`, thriftReference.value, thriftReference.capiType)

}
