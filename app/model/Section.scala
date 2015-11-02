package model

import com.amazonaws.services.dynamodbv2.document.Item
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsValue, Json, JsPath, Format}

import scala.util.control.NonFatal

case class Section(
                    id: Long,
                    sectionTagId: Long,
                    name: String,
                    path: String,
                    wordsForUrl: String,
                    pageId: Long,
                    editions: Map[String, EditionalisedPage] = Map()
                    )

object Section {

  implicit val sectionFormat: Format[Section] = (
      (JsPath \ "id").format[Long] and
      (JsPath \ "sectionTagId").format[Long] and
      (JsPath \ "name").format[String] and
      (JsPath \ "path").format[String] and
      (JsPath \ "wordsForUrl").format[String] and
      (JsPath \ "pageId").format[Long] and
      (JsPath \ "editions").formatNullable[Map[String, EditionalisedPage]].inmap[Map[String, EditionalisedPage]](_.getOrElse(Map()), Some(_))
    )(Section.apply, unlift(Section.unapply))

  def fromItem(item: Item) = try{
    Json.parse(item.toJSON).as[Section]
  } catch {
    case NonFatal(e) => Logger.error(s"failed to load section ${item.toJSON}", e); throw e

  }

  def fromJson(json: JsValue) = json.as[Section]

}
