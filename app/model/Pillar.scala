package model

import com.amazonaws.services.dynamodbv2.document.Item
import ai.x.play.json.Jsonx
import ai.x.play.json.Encoders.encoder
import play.api.Logger
import play.api.libs.json.{Format, Json}
import com.gu.tagmanagement.{Pillar => ThriftPillar}

import scala.util.control.NonFatal

case class Pillar(id: Long, path: String, name: String, sectionIds: Seq[String], pageId: Long)

object Pillar {
  implicit val pillarFormat: Format[Pillar] = Jsonx.formatCaseClassUseDefaults[Pillar]

  def toItem(pillar: Pillar): Item = Item.fromJSON(Json.toJson(pillar).toString())

  def fromItem(item: Item): Pillar = try {
    Json.parse(item.toJSON).as[Pillar]
  } catch {
    case NonFatal(e) => Logger.error(s"failed to load pillar ${item.toJSON}", e); throw e
  }

  def asThrift(pillar: Pillar) = ThriftPillar(
    id = pillar.id,
    path = pillar.path,
    name = pillar.name,
    sectionIds = pillar.sectionIds
  )
}
