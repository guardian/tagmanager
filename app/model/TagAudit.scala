package model

import com.amazonaws.services.dynamodbv2.document.Item
import com.gu.pandomainauth.model.User
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

  def created(tag: Tag)(implicit username: Option[String] = None): TagAudit = {
    TagAudit(tag.id, "created", new DateTime(), username.getOrElse("default user"), s"tag '${tag.internalName}' created", TagSummary(tag), None)
  }

  def updated(tag: Tag)(implicit username: Option[String] = None): TagAudit = {
    TagAudit(tag.id, "updated", new DateTime(), username.getOrElse("default user"), s"tag '${tag.internalName}' updated", TagSummary(tag), None)
  }

  def deleted(tag: Tag, username: Option[String] = None): TagAudit = {
    TagAudit(tag.id, "deleted", new DateTime(), username.getOrElse("default user"), s"tag '${tag.internalName}' deleted", TagSummary(tag), None)
  }

  def batchTag(tag: Tag, operation: String, contentCount: Int)(implicit user: Option[User] = None): TagAudit = {
    val message = operation match {
      case "remove" => s"tag '${tag.internalName}' removed from $contentCount items(s)"
      case _ => s"tag '${tag.internalName}' added to $contentCount items(s)"
    }
    TagAudit(tag.id, "batchtag", new DateTime(), user.map(_.email).getOrElse("default user"), message, TagSummary(tag), None)
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