package model;

import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument
import org.joda.time.DateTime
import helpers.JodaDateTimeFormat._
import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, JsPath, Format}
import play.api.Logging
import services.DynamoJsonConversions
import scala.util.control.NonFatal

case class AppAudit(action: String, date: DateTime, user: String, description: String) extends Audit {
  override def operation: String = action
  override def auditType = "application"
  override def resourceId = None
  override def message = None

  def toItem: EnhancedDocument = DynamoJsonConversions.jsonToDocument(Json.toJson(this))
}

object AppAudit extends Logging {
  implicit val appAuditFormat: Format[AppAudit] = (
    (JsPath \ "action").format[String] and
      (JsPath \ "date").format[DateTime] and
      (JsPath \ "user").format[String] and
      (JsPath \ "description").format[String]
    )(AppAudit.apply, unlift(AppAudit.unapply))

  def fromItem(item: EnhancedDocument): AppAudit = try {
    Json.parse(item.toJson()).as[AppAudit]
  } catch {
    case NonFatal(e) => {
      logger.error(s"failed to load app Audit ${item.toJson()}", e)
      throw e
    }
  }

  def reindexTags()(implicit username: Option[String] = None): AppAudit = {
    AppAudit("reindexTags", new DateTime(), username.getOrElse("unknown"), "tag reindex started");
  }

  def reindexSections()(implicit username: Option[String] = None): AppAudit = {
    AppAudit("reindexSections", new DateTime(), username.getOrElse("unknown"), "section reindex started");
  }

  def reindexPillars()(implicit username: Option[String] = None): AppAudit = {
    AppAudit("reindexPillars", new DateTime(), username.getOrElse("unknown"), "pillar reindex started");
  }
}
