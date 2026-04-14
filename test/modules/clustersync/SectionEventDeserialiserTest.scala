package modules.clustersync

import software.amazon.kinesis.retrieval.KinesisClientRecord
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

    val testRecordWithCompression = KinesisClientRecord.builder()
      .data(ByteBuffer.wrap(thriftKinesisEvent))
      .build()

    SectionEventDeserialiser.deserialise(testRecordWithCompression) shouldBe Success(sectionUpdateEvent)

  }

}
