package model

import com.amazonaws.services.dynamodbv2.document.Item
import ai.x.play.json.Jsonx
import ai.x.play.json.Encoders.encoder
import ai.x.play.json.implicits.optionWithNull
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsValue, Json, JsPath, Format}
import com.gu.tagmanagement.{Section => ThriftSection}

import scala.util.control.NonFatal

case class ExternalReferenceType (
                    typeName: String,
                    displayName: String,
                    path: String,
                    capiType: Option[String]
                  )

object ExternalReferenceType {

  implicit val sectionFormat = Jsonx.formatCaseClass[ExternalReferenceType]

  def fromItem(item: Item) = try{
    Json.parse(item.toJSON).as[ExternalReferenceType]
  } catch {
    case NonFatal(e) => Logger.error(s"failed to load external reference type ${item.toJSON}", e); throw e
  }

  def fromJson(json: JsValue) = json.as[ExternalReferenceType]

}
