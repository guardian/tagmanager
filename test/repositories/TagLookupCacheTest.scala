package repositories

import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}
import utils.TagTestUtils._

class TagLookupCacheTest extends FlatSpec with BeforeAndAfterEach with Matchers {

  private val cache = TagLookupCache

  override def beforeEach(): Unit = cache.allTags.set(Nil)

  behavior of "TagLookupCache.insertTag()"

  it should "insert new tag to cache" in {

    val testTag = createTestTag("test", 1, 0)
    cache.insertTag(testTag)

    cache.allTags.get.contains(testTag) shouldEqual true
  }

  it should "update the tag if the cache have older version of tag" in {
    val tagId = 1
    val testTag = createTestTag("test", tagId, 0)

    cache.insertTag(testTag)

    cache.allTags.get.find(_.id == tagId).get shouldEqual testTag

    val testTagAfterUpdate = createTestTag("test updated", tagId, 1)

    cache.insertTag(testTagAfterUpdate)
    cache.allTags.get.size shouldEqual 1
    cache.allTags.get.find(_.id == tagId).get shouldEqual testTagAfterUpdate
  }

  it should "do not update the tag if the cache have already more up to date tag version" in {
    val tagId = 1
    val mostRecentTag = createTestTag("test", tagId, 1)
    cache.insertTag(mostRecentTag)

    val notUpToDateTestTagForUpdateRequestWith = createTestTag("test updated", tagId, 0)

    cache.insertTag(notUpToDateTestTagForUpdateRequestWith)
    cache.allTags.get.size shouldEqual 1
    cache.allTags.get.find(_.id == tagId).get shouldEqual mostRecentTag
  }

}
