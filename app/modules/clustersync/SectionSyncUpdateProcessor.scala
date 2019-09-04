package modules.clustersync

import com.amazonaws.services.kinesis.model.Record
import com.gu.tagmanagement.{EventType, SectionEvent}
import model.Section
import play.api.Logger
import repositories.SectionLookupCache
import services.KinesisStreamRecordProcessor

import scala.util.{Failure, Success, Try}

object SectionEventDeserialiser {

  import services.KinesisRecordPayloadConversions._

  def deserialise(record: Record): Try[SectionEvent] = {
    val tProto = kinesisRecordAsThriftCompactProtocol(record, stripCompressionByte = true)
    Try(SectionEvent.decode(tProto))
  }

}

object SectionSyncUpdateProcessor extends KinesisStreamRecordProcessor {

  override def process(record: Record): Unit = {
    SectionEventDeserialiser.deserialise(record) match {
      case Success(sectionEvent) => updateSectionLookupCache(sectionEvent)
      case Failure(exp) =>
        Logger.error(s"issue while SectionEvent decode:\n ${exp.getMessage}")
    }

  }

  private def updateSectionLookupCache(sectionEvent: SectionEvent) {
    sectionEvent.eventType match {
      case EventType.Update =>
        Logger.info(s"inserting updated section ${sectionEvent.sectionId} into lookup cache")
        sectionEvent.section.foreach { s => SectionLookupCache.insertSection(Section(s)) }
      case EventType.Delete =>
        Logger.info(s"removing section ${sectionEvent.sectionId} from lookup cache")
        SectionLookupCache.removeSection(sectionEvent.sectionId)
      case et =>
        Logger.warn(s"unrecognised event type ${et.name}")
    }
  }
}
