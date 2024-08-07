package modules.clustersync

import com.amazonaws.services.kinesis.model.Record
import com.gu.tagmanagement.{EventType, SectionEvent}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import services.ThriftSerializer

import java.nio.ByteBuffer
import scala.util.Success

class SectionEventDeserialiserTest extends AnyFlatSpec with Matchers {

  it should "serialise byte stream from Kinesis record" in {

    val sectionUpdateEvent = SectionEvent(EventType.Update, 1L, None)

    val thriftKinesisEvent: Array[Byte] = ThriftSerializer.serializeToBytes(sectionUpdateEvent, true)

    val testRecordWithCompression = new Record().withData(ByteBuffer.wrap(thriftKinesisEvent))

    SectionEventDeserialiser.deserialise(testRecordWithCompression) shouldBe Success(sectionUpdateEvent)

  }

}
