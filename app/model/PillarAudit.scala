package model

import com.amazonaws.services.dynamodbv2.document.Item
import com.gu.auditing.model.v1.{App, Notification}
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath, Json}

import scala.util.control.NonFatal


case class PillarAudit(
                        pillarId: Long,
                        operation: String,
                        date: DateTime,
                        user: String,
                        description: String,
                        pillar: Pillar
                       ) extends Audit {
  override def auditType = "pillar"

  def toItem = Item.fromJSON(Json.toJson(this).toString())
}

object PillarAudit {

  implicit val pillarAuditFormat: Format[PillarAudit] = (
    (JsPath \ "pillarId").format[Long] and
      (JsPath \ "operation").format[String] and
      (JsPath \ "date").format[DateTime] and
      (JsPath \ "user").format[String] and
      (JsPath \ "description").format[String] and
      (JsPath \ "pillar").format[Pillar]
    )(PillarAudit.apply, unlift(PillarAudit.unapply))

  def fromItem(item: Item) = try {
    Json.parse(item.toJSON).as[PillarAudit]
  } catch {
    case NonFatal(e) => {
      Logger.error(s"failed to load pillar Audit ${item.toJSON}", e)
      throw e
    }
  }

  def created(pillar: Pillar)(implicit user: Option[String] = None): PillarAudit = {
    PillarAudit(pillar.id, "created", new DateTime(), user.getOrElse("default user"), s"pillar '${pillar.name}' created", pillar)
  }

  def updated(pillar: Pillar)(implicit user: Option[String] = None): PillarAudit = {
    PillarAudit(pillar.id, "updated", new DateTime(), user.getOrElse("default user"), s"pillar '${pillar.name}' updated", pillar)
  }
}
