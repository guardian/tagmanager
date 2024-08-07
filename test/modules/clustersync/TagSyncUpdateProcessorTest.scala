package modules.clustersync

import com.amazonaws.services.kinesis.model.Record
import com.gu.tagmanagement.{EventType, TagEvent}
import model.BlockingLevel
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import repositories.TagLookupCache
import services.ThriftSerializer
import utils.TagTestUtils._

import java.nio.ByteBuffer

class TagSyncUpdateProcessorTest extends AnyFlatSpec with Matchers {

  behavior of "TagSyncUpdateProcessor.process()"

  it should "process consumption of Kinesis Event that Tag was Updated" +
    "and be able to serialise correctly all BlockingLevel enum fields" in {

    List(
      createTestTag("test1", 1, 0,
        tAdBlockingLevel = Some(BlockingLevel.SUGGEST),
        tContributionBlockingLevel = Some(BlockingLevel.SUGGEST)),
      createTestTag("test2", 2, 0,
        tAdBlockingLevel = Some(BlockingLevel.NONE),
        tContributionBlockingLevel = Some(BlockingLevel.NONE)),
      createTestTag("test3", 3, 0,
        tAdBlockingLevel = Some(BlockingLevel.FORCE),
        tContributionBlockingLevel = Some(BlockingLevel.FORCE))
    ).foreach(testTag => {
      val kinesisRecord = createKinesisTagUpdateEventRecord(testTag)

      TagSyncUpdateProcessor.process(kinesisRecord)

      TagLookupCache.getTag(testTag.id) shouldEqual Some(testTag)
    })
  }

  private def createKinesisTagUpdateEventRecord(tag: model.Tag) = {
    val thriftTag = tag.asThrift
    val tagUpdateEvent = TagEvent(EventType.Update, 1L, Some(thriftTag))
    val thriftKinesisEvent: Array[Byte] = ThriftSerializer.serializeToBytes(tagUpdateEvent, true)
    new Record()
      .withData(ByteBuffer.wrap(thriftKinesisEvent))

  }

}
