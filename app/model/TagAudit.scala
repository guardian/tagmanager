package model

import com.amazonaws.services.dynamodbv2.document.Item
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, JsPath, Format}
import repositories.SectionRepository

import scala.util.control.NonFatal


case class TagAudit(
  tagId: Long,
  operation: String,
  date: DateTime,
  user: String,
  description: String,
  tagSummary: TagSummary,
  secondaryTagSummary: Option[TagSummary]
) {
  def toItem = Item.fromJSON(Json.toJson(this).toString())
}

object TagAudit {

  implicit val tagAuditFormat: Format[TagAudit] = (
    (JsPath \ "tagId").format[Long] and
      (JsPath \ "operation").format[String] and
      (JsPath \ "date").format[DateTime] and
      (JsPath \ "user").format[String] and
      (JsPath \ "description").format[String] and
      (JsPath \ "tagSummary").format[TagSummary] and
      (JsPath \ "secondaryTagSummary").formatNullable[TagSummary]
    )(TagAudit.apply, unlift(TagAudit.unapply))

  def fromItem(item: Item) = try {
    Json.parse(item.toJSON).as[TagAudit]
  } catch {
    case NonFatal(e) => {
      Logger.error(s"failed to load tag Audit ${item.toJSON}", e)
      throw e
    }
  }
}

case class TagSummary(
  tagId: Long,
  internalName: String,
  externalName: String,
  slug: String,
  `type`: String,
  sectionName: String
)

object TagSummary {

  implicit val tagAuditFormat: Format[TagSummary] = (
    (JsPath \ "tagId").format[Long] and
      (JsPath \ "internalName").format[String] and
      (JsPath \ "externalName").format[String] and
      (JsPath \ "slug").format[String] and
      (JsPath \ "type").format[String] and
      (JsPath \ "sectionName").format[String]
    )(TagSummary.apply, unlift(TagSummary.unapply))

  def apply(tag: Tag): TagSummary =
    new TagSummary(
      tagId = tag.id,
      internalName = tag.internalName,
      externalName = tag.externalName,
      slug = tag.slug,
      `type` = tag.`type`,
      sectionName = tag.section.flatMap{ sid => SectionRepository.getSection(sid).map(_.name)}.getOrElse("global")
    )
}
