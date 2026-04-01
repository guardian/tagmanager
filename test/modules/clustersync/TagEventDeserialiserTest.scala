package modules.clustersync

import software.amazon.kinesis.retrieval.KinesisClientRecord
import com.gu.tagmanagement.{EventType, TagEvent}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import services.ThriftSerializer

import java.nio.ByteBuffer
import scala.util.Success

class TagEventDeserialiserTest extends AnyFlatSpec with Matchers  {

  it should  "serialise byte stream from Kinesis record" in {

    val tagUpdateEvent = TagEvent(EventType.Update, 1L, None)

    val thriftKinesisEvent: Array[Byte] = ThriftSerializer.serializeToBytes(tagUpdateEvent, true)

    val testRecordWithCompression = KinesisClientRecord.builder()
      .data(ByteBuffer.wrap(thriftKinesisEvent))
      .build()

    TagEventDeserialiser.deserialise(testRecordWithCompression) shouldBe Success(tagUpdateEvent)

  }

}
