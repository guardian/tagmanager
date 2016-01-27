package modules.clustersync

import com.amazonaws.services.kinesis.model.Record
import com.gu.tagmanagement.{EventType, SectionEvent}
import model.Section
import play.api.Logger
import repositories.SectionLookupCache
import services.KinesisStreamRecordProcessor

object SectionSyncUpdateProcessor extends KinesisStreamRecordProcessor {

  import services.KinesisRecordPayloadConversions._

  override def process(record: Record) {
    val sectionEvent = SectionEvent.decode(record)

    sectionEvent.eventType match {
      case EventType.Update => {
        Logger.info(s"inserting updated section ${sectionEvent.sectionId} into lookup cache")
        sectionEvent.section.foreach { s => SectionLookupCache.insertSection(Section(s)) }
      }
      case EventType.Delete => {
        Logger.info(s"removing section ${sectionEvent.sectionId} from lookup cache")
        SectionLookupCache.removeSection(sectionEvent.sectionId)
      }
      case et => {
        Logger.warn(s"unrecognised event type ${et.name}")
      }
    }
  }
}
