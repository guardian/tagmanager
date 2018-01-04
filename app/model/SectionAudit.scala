package model

import com.amazonaws.services.dynamodbv2.document.Item
import com.gu.auditing.model.v1.{App, Notification}
import com.gu.pandomainauth.model.User
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath, Json}
import repositories.SectionRepository

import scala.util.control.NonFatal


case class SectionAudit(
                     sectionId: Long,
                     operation: String,
                     date: DateTime,
                     user: String,
                     description: String,
                     sectionSummary: SectionSummary
                   ) {
  def toItem = Item.fromJSON(Json.toJson(this).toString())
}

object SectionAudit {

  implicit val sectionAuditFormat: Format[SectionAudit] = (
    (JsPath \ "sectionId").format[Long] and
      (JsPath \ "operation").format[String] and
      (JsPath \ "date").format[DateTime] and
      (JsPath \ "user").format[String] and
      (JsPath \ "description").format[String] and
      (JsPath \ "sectionSummary").format[SectionSummary]
    )(SectionAudit.apply, unlift(SectionAudit.unapply))

  def fromItem(item: Item) = try {
    Json.parse(item.toJSON).as[SectionAudit]
  } catch {
    case NonFatal(e) => {
      Logger.error(s"failed to load section Audit ${item.toJSON}", e)
      throw e
    }
  }

  def created(section: Section)(implicit user: Option[String] = None): SectionAudit = {
    SectionAudit(section.id, "created", new DateTime(), user.getOrElse("default user"), s"section '${section.name}' created", SectionSummary(section))
  }

  def updated(section: Section)(implicit user: Option[String] = None): SectionAudit = {
    SectionAudit(section.id, "updated", new DateTime(), user.getOrElse("default user"), s"section '${section.name}' updated", SectionSummary(section))
  }

  def addedEdition(section: Section, editionName: String)(implicit user: Option[String] = None): SectionAudit = {
    SectionAudit(section.id, "added edition", new DateTime(), user.getOrElse("default user"), s"added ${editionName} edition to section '${section.name}", SectionSummary(section))
  }

  def removedEdition(section: Section, editionName: String)(implicit user: Option[String] = None): SectionAudit = {
    SectionAudit(section.id, "removed edition", new DateTime(), user.getOrElse("default user"), s"removed ${editionName} edition from section '${section.name}", SectionSummary(section))
  }
}

case class SectionSummary(
                       sectionId: Long,
                       name: String,
                       path: String,
                       editions: String
                     )

object SectionSummary {

  implicit val tagAuditFormat: Format[SectionSummary] = (
    (JsPath \ "sectionId").format[Long] and
      (JsPath \ "name").format[String] and
      (JsPath \ "path").format[String] and
      (JsPath \ "editions").format[String]
    )(SectionSummary.apply, unlift(SectionSummary.unapply))

  def apply(section: Section): SectionSummary =
    new SectionSummary(
      sectionId = section.id,
      name = section.name,
      path = section.path,
      editions = section.editions.keys.mkString(" ") match {
        case "" => "No Editions"
        case e => e
      }
    )
}
