package modules.clustersync

import software.amazon.kinesis.retrieval.KinesisClientRecord
import com.gu.tagmanagement.{EventType, TagEvent}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import services.ThriftSerializer

import java.nio.ByteBuffer
import scala.util.Success

class TagEventDeserialiserTest extends AnyFlatSpec with Matchers {

  private val tagUpdateEvent = TagEvent(EventType.Update, 1L, None)
  private val thriftKinesisEvent: Array[Byte] = ThriftSerializer.serializeToBytes(tagUpdateEvent, includeCompressionFlag = true)

  private def recordWithBuffer(buf: ByteBuffer): KinesisClientRecord =
    KinesisClientRecord.builder().data(buf).build()

  it should "serialise byte stream from Kinesis record" in {
    val testRecordWithCompression = KinesisClientRecord.builder()
      .data(ByteBuffer.wrap(thriftKinesisEvent))
      .build()

    TagEventDeserialiser.deserialise(testRecordWithCompression) shouldBe Success(tagUpdateEvent)
  }

  it should "deserialise a sliced ByteBuffer (non-zero arrayOffset, as produced by KCL v2 buffer pooling in production)" in {
    // Simulate KCL v2 handing back a slice of a larger pooled buffer.
    // .slice() produces a buffer with arrayOffset > 0; .array() on this returns
    // the full backing array from index 0, which is the bug this test guards against.
    val padded = Array[Byte](0x00.toByte) ++ thriftKinesisEvent
    val slicedBuffer = ByteBuffer.wrap(padded, 1, thriftKinesisEvent.length).slice()
    TagEventDeserialiser.deserialise(recordWithBuffer(slicedBuffer)) shouldBe Success(tagUpdateEvent)
  }

  it should "deserialise a ByteBuffer with a non-zero position (as produced by KCL v2 concurrent shard reads in production)" in {
    // Simulate KCL v2 advancing the position into a larger buffer before handing it to the processor.
    val padded = Array[Byte](0x00.toByte) ++ thriftKinesisEvent
    val positionedBuffer = ByteBuffer.wrap(padded)
    positionedBuffer.position(1)
    TagEventDeserialiser.deserialise(recordWithBuffer(positionedBuffer)) shouldBe Success(tagUpdateEvent)
  }

}
