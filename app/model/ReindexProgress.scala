package model;

import com.amazonaws.services.dynamodbv2.document.Item
import org.cvogt.play.json.Jsonx
import play.api.libs.json._
import play.api.Logger
import play.json.extra.JsonFormat;
import play.json.extra.Picklers._
import scala.util.control.NonFatal

case class ReindexProgress(`type`: String, status: String, docsSent: Int, docsExpected: Int) {
  def toItem() = Item.fromJSON(Json.toJson(this).toString())
  def toCapiForm() = CapiReindexProgress(status, docsSent, docsExpected)
}

object ReindexProgress {
  val TagTypeName = "tag"
  val SectionTypeName = "section"

  val InProgress = "in progress"
  val Failed = "failed"
  val Completed = "completed"
  val Cancelled = "cancelled"
  val Unknown = "unknown"

  implicit val reindexProgressFormat: Format[ReindexProgress] = Jsonx.formatCaseClassUseDefaults[ReindexProgress]

  def fromJson(json: JsValue) = json.as[Tag]
  def fromItem(item: Item) = try {
    Json.parse(item.toJSON).as[ReindexProgress]
  } catch {
    case NonFatal(e) => {
      Logger.error(s"failed to load reindex progress ${item.toJSON}")
      throw e
    }
  }

  def unknownTag() = {
    ReindexProgress(TagTypeName, Unknown, 0, 0)
  }

  def unknownSection() = {
    ReindexProgress(SectionTypeName, Unknown, 0, 0)
  }

  def resetTag(docsExpected: Int) = {
    ReindexProgress(TagTypeName, InProgress, 0, docsExpected)
  }

  def resetSection(docsExpected: Int) = {
    ReindexProgress(SectionTypeName, InProgress, 0, docsExpected)
  }

  def progressTag(docsSent: Int, docsExpected: Int) = {
    ReindexProgress(TagTypeName, InProgress, docsSent, docsExpected)
  }

  def progressSection(docsSent: Int, docsExpected: Int) = {
    ReindexProgress(SectionTypeName, InProgress, docsSent, docsExpected)
  }

  def completeTag(docsSent: Int, docsExpected: Int) = {
    ReindexProgress(TagTypeName, Completed, docsSent, docsExpected)
  }

  def completeSection(docsSent: Int, docsExpected: Int) = {
    ReindexProgress(SectionTypeName, Completed, docsSent, docsExpected)
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
