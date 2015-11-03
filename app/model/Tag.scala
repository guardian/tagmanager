package model

import com.amazonaws.services.dynamodbv2.document.Item
import play.api.Logger
import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.util.control.NonFatal

case class Tag(
  id: Long,
  path: String,
  pageId: Long,
  `type`: String,
  internalName: String,
  externalName: String,
  slug: String,
  hidden: Boolean = false,
  legallySensitive: Boolean = false,
  comparableValue: String,
  section: Long,
  description: Option[String] = None,
  parents: Set[Long] = Set(),
  references: List[Reference] = Nil
)

object Tag {

  implicit val tagFormat: Format[Tag] = (
      (JsPath \ "id").format[Long] and
      (JsPath \ "path").format[String] and
      (JsPath \ "pageId").format[Long] and
      (JsPath \ "type").format[String] and
      (JsPath \ "internalName").format[String] and
      (JsPath \ "externalName").format[String] and
      (JsPath \ "slug").format[String] and
      (JsPath \ "hidden").format[Boolean] and
      (JsPath \ "legallySensitive").format[Boolean] and
      (JsPath \ "comparableValue").format[String] and
      (JsPath \ "section").format[Long] and
      (JsPath \ "description").formatNullable[String] and
      (JsPath \ "parents").formatNullable[Set[Long]].inmap[Set[Long]](_.getOrElse(Set()), Some(_)) and
      (JsPath \ "externalReferences").formatNullable[List[Reference]].inmap[List[Reference]](_.getOrElse(Nil), Some(_))
    )(Tag.apply, unlift(Tag.unapply))

  def fromItem(item: Item) = try{
    Json.parse(item.toJSON).as[Tag]
  } catch {
    case NonFatal(e) => Logger.error(s"failed to load tag ${item.toJSON}", e); throw e

  }
  
  def fromJson(json: JsValue) = json.as[Tag]

}

