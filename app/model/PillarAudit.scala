package model

import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument
import org.joda.time.DateTime
import helpers.JodaDateTimeFormat._
import play.api.Logging
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath, Json}
import services.DynamoJsonConversions

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
  override def resourceId = Some(pillarId.toString)
  override def message = None

  def toItem: EnhancedDocument = DynamoJsonConversions.jsonToDocument(Json.toJson(this))
}

object PillarAudit extends Logging {

  implicit val pillarAuditFormat: Format[PillarAudit] = (
    (JsPath \ "pillarId").format[Long] and
      (JsPath \ "operation").format[String] and
      (JsPath \ "date").format[DateTime] and
      (JsPath \ "user").format[String] and
      (JsPath \ "description").format[String] and
      (JsPath \ "pillar").format[Pillar]
    )(PillarAudit.apply, unlift(PillarAudit.unapply))

  def fromItem(item: EnhancedDocument): PillarAudit = try {
    Json.parse(item.toJson()).as[PillarAudit]
  } catch {
    case NonFatal(e) => {
      logger.error(s"failed to load pillar Audit ${item.toJson()}", e)
      throw e
    }
  }

  def created(pillar: Pillar)(implicit user: Option[String]): PillarAudit = {
    PillarAudit(pillar.id, "created", new DateTime(), user.getOrElse("unknown"), s"pillar '${pillar.name}' created", pillar)
  }

  def updated(pillar: Pillar)(implicit user: Option[String]): PillarAudit = {
    PillarAudit(pillar.id, "updated", new DateTime(), user.getOrElse("unknown"), s"pillar '${pillar.name}' updated", pillar)
  }
}
