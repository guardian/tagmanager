package model

import play.api.libs.json._
import play.api.libs.functional.syntax._
import com.gu.tagmanagement.{Reference => ThriftReference}


case class Reference(`type`: String, value: String) {
  def asThrift = ThriftReference(`type`, value)
  def asXml = {<reference>
               <type>{this.`type`}</type>
               <value>{this.value}</value>
               </reference>}
}

object Reference {

  implicit val referenceFormat: Format[Reference] = (
      (JsPath \ "type").format[String] and
      (JsPath \ "value").format[String]
    )(Reference.apply, unlift(Reference.unapply))

  def apply(thriftReference: ThriftReference): Reference = Reference(thriftReference.`type`, thriftReference.value)

}
