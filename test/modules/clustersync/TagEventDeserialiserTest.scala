package modules.clustersync

import java.nio.ByteBuffer

import com.amazonaws.services.kinesis.model.Record
import com.gu.tagmanagement.{EventType, TagEvent}
import org.scalatest.{FlatSpec, FunSuite, Matchers, TryValues}
import services.ThriftSerializer

import scala.util.Success

class TagEventDeserialiserTest extends  FlatSpec with Matchers  {

  it should  "serialise byte stream from Kinesis record" in {

    val tagUpdateEvent = TagEvent(EventType.Update, 1L, None)

    val thriftKinesisEvent: Array[Byte] = ThriftSerializer.serializeToBytes(tagUpdateEvent, true)

    val testRecordWithCompression = new Record().withData(ByteBuffer.wrap(thriftKinesisEvent))

    TagEventDeserialiser.deserialise(testRecordWithCompression) shouldBe Success(tagUpdateEvent)

  }

}
