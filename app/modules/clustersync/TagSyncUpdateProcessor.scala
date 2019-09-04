package modules.clustersync

import com.amazonaws.services.kinesis.model.Record
import com.gu.tagmanagement.{EventType, TagEvent}
import play.api.Logger
import repositories.{TagLookupCache, TagRepository}
import services.KinesisStreamRecordProcessor

import scala.util.{Failure, Success, Try}

object TagEventDeserialiser {

  import services.KinesisRecordPayloadConversions._

  def deserialise(record: Record): Try[TagEvent] = {
    val tProto = kinesisRecordAsThriftCompactProtocol(record, stripCompressionByte = true)
    Try(TagEvent.decode(tProto))
  }

}

object TagSyncUpdateProcessor extends KinesisStreamRecordProcessor {

  override def process(record: Record) {
    Logger.info(s"Kinesis consumer receives record \n $record")
    TagEventDeserialiser.deserialise(record) match {
      case Success(tagEvent) => updateTagsLookupCache(tagEvent)
      case Failure(exp) =>
        Logger.error(s"issue while TagEvent decode:\n ${exp.getMessage}")
    }
  }

  private def updateTagsLookupCache(tagEvent: TagEvent): Unit = {
    Logger.error(s"TagEvent received: \n $tagEvent")
    tagEvent.eventType match {
      case EventType.Update =>
        Logger.info(s"inserting updated tag ${tagEvent.tagId} into lookup cache")

        tagEvent.tag match {
          case Some(t) =>
            TagRepository
              .getTag(t.id)
              .foreach(tagFromDB => TagLookupCache.insertTag(tagFromDB))
          case None =>
            Logger.warn(s"TagEvent for ${tagEvent.tagId} did not contain any tag object")
        }
      case EventType.Delete =>
        Logger.info(s"removing tag ${tagEvent.tagId} from lookup cache")
        TagLookupCache.removeTag(tagEvent.tagId)
      case et =>
        Logger.warn(s"unrecognised event type ${et.name}")
    }
  }
}
