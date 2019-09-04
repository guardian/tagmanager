package modules.clustersync

import java.nio.ByteBuffer

import com.amazonaws.services.kinesis.model.Record
import com.gu.tagmanagement.{EventType, TagEvent}
import model.BlockingLevel
import org.scalatest.{FlatSpec, Matchers}
import repositories.TagLookupCache
import services.ThriftSerializer
import utils.TagTestUtils._

class TagSyncUpdateProcessorTest extends FlatSpec with Matchers {

  behavior of "TagSyncUpdateProcessor.process()"

  it should "process Consumntion of Kinesis Event that Tag was Updated" +
    "and be able to serialise correctly all tag enum fields" in {

    val testTag = createTestTag("test", 1, 0,
      tAdBlockingLevel = Some(BlockingLevel.Suggest),
      tContributionBlockingLevel = Some(BlockingLevel.Suggest))
    val tagUpdateEvent = TagEvent(EventType.Update, 1L, Some(testTag.asThrift))
    val thriftKinesisEvent: Array[Byte] = ThriftSerializer.serializeToBytes(tagUpdateEvent, true)

    val r = new Record().withData(ByteBuffer.wrap(thriftKinesisEvent))

    TagSyncUpdateProcessor.process(r)

    TagLookupCache.getTag(testTag.id) shouldEqual Some(testTag)

  }

}
