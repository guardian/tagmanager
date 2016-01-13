package model.jobs

import model.Tag
import play.api.libs.functional.syntax._
import play.api.libs.json._
import repositories._
import services.{Config, KinesisStreams}

case class ReindexTags(capiJobId: String) extends Step {
  override def process: Option[Step] = {
    TagLookupCache.allTags.get.grouped(Config().reindexBatchSize).foreach { tags =>
      KinesisStreams.reindexStream.publishUpdate("reindex" + capiJobId, Tag.createReindexBatch(tags))
    }
    None
  }
}

object ReindexTags {
  //Weird inmapping required because of a "limitation" in the macro system in play. Meaning it doesn't allow for
  //single field case classes to be serialized using the Format
  //http://stackoverflow.com/questions/14754092/how-to-turn-json-to-case-class-when-case-class-has-only-one-field
  implicit val reindexTagsFormat: Format[ReindexTags] = (
    JsPath \ "capiJobId"
  ).format[String].inmap(id => ReindexTags(id), (reindexTags: ReindexTags) => reindexTags.capiJobId)
}
