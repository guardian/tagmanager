package model;

import com.amazonaws.services.dynamodbv2.document.Item
import ai.x.play.json.Jsonx
import ai.x.play.json.Encoders.encoder
import play.api.libs.json._
import play.api.Logging
import scala.util.control.NonFatal

case class ReindexProgress(`type`: String, status: String, docsSent: Int, docsExpected: Int) {
  def toItem() = Item.fromJSON(Json.toJson(this).toString())
  def toCapiForm() = CapiReindexProgress(status, docsSent, docsExpected)
}

object ReindexProgress extends Logging {
  val TagTypeName = "tag"
  val SectionTypeName = "section"
  val PillarTypeName = "pillar"

  val InProgress = "in progress"
  val Failed = "failed"
  val Completed = "completed"
  val Cancelled = "cancelled" // Not used yet.

  implicit val reindexProgressFormat: Format[ReindexProgress] = Jsonx.formatCaseClassUseDefaults[ReindexProgress]

  def fromJson(json: JsValue) = json.as[Tag]
  def fromItem(item: Item) = try {
    Json.parse(item.toJSON).as[ReindexProgress]
  } catch {
    case NonFatal(e) => {
      logger.error(s"failed to load reindex progress ${item.toJSON}")
      throw e
    }
  }

  def resetTag(docsExpected: Int) = {
    ReindexProgress(TagTypeName, InProgress, 0, docsExpected)
  }

  def resetSection(docsExpected: Int) = {
    ReindexProgress(SectionTypeName, InProgress, 0, docsExpected)
  }

  def resetPillar(docsExpected: Int) = {
    ReindexProgress(PillarTypeName, InProgress, 0, docsExpected)
  }

  def progressTag(docsSent: Int, docsExpected: Int) = {
    ReindexProgress(TagTypeName, InProgress, docsSent, docsExpected)
  }

  def progressSection(docsSent: Int, docsExpected: Int) = {
    ReindexProgress(SectionTypeName, InProgress, docsSent, docsExpected)
  }

  def progressPillar(docsSent: Int, docsExpected: Int) = {
    ReindexProgress(PillarTypeName, InProgress, docsSent, docsExpected)
  }

  def completeTag(docsSent: Int, docsExpected: Int) = {
    ReindexProgress(TagTypeName, Completed, docsSent, docsExpected)
  }

  def completeSection(docsSent: Int, docsExpected: Int) = {
    ReindexProgress(SectionTypeName, Completed, docsSent, docsExpected)
  }

  def completePillar(docsSent: Int, docsExpected: Int) = {
    ReindexProgress(PillarTypeName, Completed, docsSent, docsExpected)
  }

  def failTag(docsSent: Int, docsExpected: Int) = {
    ReindexProgress(TagTypeName, Failed, docsSent, docsExpected)
  }

  def failSection(docsSent: Int, docsExpected: Int) = {
    ReindexProgress(SectionTypeName, Failed, docsSent, docsExpected)
  }

  def failPillar(docsSent: Int, docsExpected: Int) = {
    ReindexProgress(PillarTypeName, Failed, docsSent, docsExpected)
  }
}

// Version of the reindex progress without the type string
case class CapiReindexProgress(status: String, documentsIndexed: Int, documentsExpected: Int) {
  def toItem = Item.fromJSON(Json.toJson(this).toString())
  def toJson = Json.toJson(this)
}

object CapiReindexProgress {
  implicit val capiReindexProgressFormat: Format[CapiReindexProgress] = Jsonx.formatCaseClassUseDefaults[CapiReindexProgress]
}
