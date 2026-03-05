package model

import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument
import ai.x.play.json.Jsonx
import ai.x.play.json.Encoders.encoder
import play.api.Logging
import play.api.libs.json.{Format, Json}
import services.DynamoJsonConversions
import com.gu.tagmanagement.{Pillar => ThriftPillar}

import scala.util.control.NonFatal

case class Pillar(id: Long, path: String, name: String, sectionIds: Seq[String], pageId: Long)

object Pillar extends Logging {
  implicit val pillarFormat: Format[Pillar] = Jsonx.formatCaseClassUseDefaults[Pillar]

  def toItem(pillar: Pillar): EnhancedDocument = DynamoJsonConversions.jsonToDocument(Json.toJson(pillar))

  def fromItem(item: EnhancedDocument): Pillar = try {
    Json.parse(item.toJson()).as[Pillar]
  } catch {
    case NonFatal(e) => logger.error(s"failed to load pillar ${item.toJson()}", e); throw e
  }

  def asThrift(pillar: Pillar) = ThriftPillar(
    id = pillar.id,
    path = pillar.path,
    name = pillar.name,
    sectionIds = pillar.sectionIds
  )
}
