package repositories

import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import utils.TagTestUtils._

class TagLookupCacheTest extends AnyFlatSpec with BeforeAndAfterEach with Matchers {

  private val cache = TagLookupCache

  private val testTagId = 1

  override def beforeEach(): Unit = cache.allTags.set(Nil)

  behavior of "TagLookupCache.insertTag()"

  it should "insert new tag to cache" in {
    val testTag = createTestTag("test", 1, 0)
    cache.allTags.get shouldEqual Nil
    cache.insertTag(testTag)

    cache.allTags.get.contains(testTag) shouldEqual true
  }

  it should "update the tag if the cache have older version of tag" in {
    val testTag = createTestTag("test", testTagId, 0)
    cache.allTags.get shouldEqual Nil

    cache.insertTag(testTag)
    cache.allTags.get.find(_.id == testTagId).get shouldEqual testTag

    val testTagAfterUpdate = createTestTag("test updated", testTagId, 1)
    cache.insertTag(testTagAfterUpdate)
    cache.allTags.get.size shouldEqual 1
    cache.allTags.get.find(_.id == testTagId).get shouldEqual testTagAfterUpdate
  }

  it should "do not update the tag if the cache have already more up to date tag version" in {
    cache.allTags.get shouldEqual Nil
    val mostRecentTag = createTestTag("test", testTagId, 1)
    cache.insertTag(mostRecentTag)

    val notUpToDateTestTagForUpdateRequestWith = createTestTag("test updated", testTagId, 0)

    cache.insertTag(notUpToDateTestTagForUpdateRequestWith)
    cache.allTags.get.size shouldEqual 1
    cache.allTags.get.find(_.id == testTagId).get shouldEqual mostRecentTag
  }

}
