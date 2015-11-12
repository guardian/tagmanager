package modules

import com.amazonaws.services.kinesis.model.Record
import com.google.inject.AbstractModule
import com.gu.tagmanagement.{EventType, TagEvent}
import model.Tag
import play.api.Logger
import repositories.TagLookupCache
import services.{KinesisConsumer, KinesisStreamRecordProcessor}

class TagUpdateSync extends AbstractModule {
  override def configure {
    Logger.info("registering sync consumer")
    new KinesisConsumer("tag-update-stream-dev", "tagmanager-sync-dev", TagUpdateProcessor)
  }
}

object TagUpdateProcessor extends KinesisStreamRecordProcessor {

  import services.KinesisRecordPayloadConversions.kinesisRecordAsThriftCompactProtocol

  override def process(record: Record) {
    val tagEvent = TagEvent.decode(record)

    tagEvent.eventType match {
      case EventType.Update => {
        Logger.info(s"inserting updated tag ${tagEvent.tagId} into lookup cache")
        tagEvent.tag.foreach { t => TagLookupCache.insertTag(Tag(t)) }
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
