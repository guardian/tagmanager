package modules.clustersync

import com.amazonaws.services.kinesis.model.Record
import com.gu.tagmanagement.{EventType, TagEvent}
import play.api.Logger
import repositories.{TagLookupCache, TagRepository}
import services.KinesisStreamRecordProcessor

import scala.util.{Failure, Success, Try}

object TagSyncUpdateProcessor extends KinesisStreamRecordProcessor {

  import services.KinesisRecordPayloadConversions._

  override def process(record: Record) {
    Try(TagEvent.decode(record)) match {
      case Success(tagEvent) => updateTagsLookupCache(tagEvent)
      case Failure(exp) =>
        Logger.error(s"issue while TagEvent decode:\n ${exp.getMessage}")
    }
  }

  private def updateTagsLookupCache(tagEvent: TagEvent) = {
    tagEvent.eventType match {
      case EventType.Update => {
        Logger.info(s"inserting updated tag ${tagEvent.tagId} into lookup cache")

        tagEvent.tag match {
          case Some(t) => {
            val tId = t.id
            val tagFromDB = TagRepository.getTag(tId)
            tagFromDB.foreach(TagLookupCache.insertTag(_))
          }
          case None =>
            Logger.warn(s"TagEvent for ${tagEvent.tagId} did not contain any tag object")
        }
      }
      case EventType.Delete => {
        Logger.info(s"removing tag ${tagEvent.tagId} from lookup cache")
        TagLookupCache.removeTag(tagEvent.tagId)
      }
      case et => {
        Logger.warn(s"unrecognised event type ${et.name}")
      }
    }
  }
}
