package modules.clustersync

import com.amazonaws.services.kinesis.model.Record
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

    val testRecordWithCompression = new Record().withData(ByteBuffer.wrap(thriftKinesisEvent))

    TagEventDeserialiser.deserialise(testRecordWithCompression) shouldBe Success(tagUpdateEvent)

  }

}
