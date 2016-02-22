package model;

import com.amazonaws.services.dynamodbv2.document.Item
import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, JsPath, Format}
import play.api.Logger
import scala.util.control.NonFatal

case class AppAudit(action: String, date: DateTime, user: String, description: String) {
  def toItem = Item.fromJSON(Json.toJson(this).toString())
}

object AppAudit {
  implicit val appAuditFormat: Format[AppAudit] = (
    (JsPath \ "action").format[String] and
      (JsPath \ "date").format[DateTime] and
      (JsPath \ "user").format[String] and
      (JsPath \ "description").format[String]
    )(AppAudit.apply, unlift(AppAudit.unapply))

  def fromItem(item: Item) = try {
    Json.parse(item.toJSON).as[AppAudit]
  } catch {
    case NonFatal(e) => {
      Logger.error(s"failed to load app Audit ${item.toJSON}", e)
      throw e
    }
  }

  def reindexTags()(implicit username: Option[String] = None): AppAudit = {
    AppAudit("reindexTags", new DateTime(), username.getOrElse("default user"), "tag reindex started");
  }

  def reindexSections()(implicit username: Option[String] = None): AppAudit = {
    AppAudit("reindexSections", new DateTime(), username.getOrElse("default user"), "section reindex started");
  }
}
