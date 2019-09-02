package repositories

import model.Tag
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}

class TagLookupCacheTest extends FlatSpec with BeforeAndAfterEach with Matchers {

  private val cache = TagLookupCache

  override def beforeEach(): Unit = cache.allTags.set(Nil)

  behavior of "TagLookupCache.insertTag"

  it should "insert new tag to cache" in {

    val testTag = generateTestTag("test", 1, 0)
    cache.insertTag(testTag)

    cache.allTags.get.contains(testTag) shouldEqual true
  }

  it should "update the tag if the cache have older version of tag" in {
    val tagId = 1
    val testTag = generateTestTag("test", tagId, 0)

    cache.insertTag(testTag)

    cache.allTags.get.find(_.id == tagId).get shouldEqual testTag

    val testTagAfterUpdate = generateTestTag("test updated", tagId, 1)

    cache.insertTag(testTagAfterUpdate)
    cache.allTags.get.size shouldEqual 1
    cache.allTags.get.find(_.id == tagId).get shouldEqual testTagAfterUpdate
  }

  it should "do not update the tag if the cache have already more up to date tag version" in {
    val tagId = 1
    val mostRecentTag = generateTestTag("test", tagId, 1)
    cache.insertTag(mostRecentTag)

    val notUpToDateTestTagForUpdateRequestWith = generateTestTag("test updated", tagId, 0)

    cache.insertTag(notUpToDateTestTagForUpdateRequestWith)
    cache.allTags.get.size shouldEqual 1
    cache.allTags.get.find(_.id == tagId).get shouldEqual mostRecentTag
  }

  private def generateTestTag(tInternalName: String, tid: Long, tUpdatedAt: Long) = {
    Tag(
      id = tid,
      path = "test",
      pageId = 1,
      `type` = "Topic",
      internalName = tInternalName,
      externalName = "",
      slug = "",
      comparableValue = "",
      section = None,
      publication = None,
      isMicrosite = false,
      trackingInformation = None,
      campaignInformation = None,
      adBlockingLevel = None,
      contributionBlockingLevel = None,
      updatedAt = tUpdatedAt
    )
  }

}
