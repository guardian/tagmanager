package utils

import model.{BlockingLevel, Tag}

object TagTestUtils {

  def createTestTag(
                       tInternalName: String, tid: Long,
                       tUpdatedAt: Long,
                       tAdBlockingLevel: Option[BlockingLevel] = None,
                       tContributionBlockingLevel: Option[BlockingLevel] = None) = {
    Tag(
      id = tid,
      path = s"test/$tInternalName",
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
      adBlockingLevel = tAdBlockingLevel,
      contributionBlockingLevel = tContributionBlockingLevel,
      updatedAt = tUpdatedAt
    )
  }

}
