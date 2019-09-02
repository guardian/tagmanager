package modules.clustersync

import com.amazonaws.services.kinesis.model.Record
import com.gu.tagmanagement.{EventType, TagEvent}
import model.Tag
import play.api.Logger
import repositories.TagLookupCache
import services.KinesisStreamRecordProcessor

object TagSyncUpdateProcessor extends KinesisStreamRecordProcessor {

  import services.KinesisRecordPayloadConversions._

  override def process(record: Record) {
    val tagEvent = TagEvent.decode(record)

    tagEvent.eventType match {
      case EventType.Update => {
        Logger.info(s"insertion fof updated tag ${tagEvent.tagId} into lookup cache started")

        tagEvent.tag match {
          case Some(t) => TagLookupCache.insertTag(Tag(t))
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
