package utils

import model._

object TagTestUtils {

  def createTestTag(
                     tInternalName: String, tid: Long,
                     tUpdatedAt: Long,
                     tAdBlockingLevel: Option[BlockingLevel] = None,
                     tContributionBlockingLevel: Option[BlockingLevel] = None): model.Tag = {

    val stubPodcastMetadata = PodcastMetadata("url")
    val stubContributorInformation = ContributorInformation(None, None, None, None, None, None, None)
    val stubPublicationInformation = PublicationInformation(Some(1L), Set())
    val stubTrackingInformation = TrackingInformation("testTType")
    val stubCampaignInformation = CampaignInformation("testCType")
    val stubPaidContentInformation = PaidContentInformation("testPCType")

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
      externalReferences = List(Reference("test", "test", None)),
      podcastMetadata = Some(stubPodcastMetadata),
      contributorInformation = Some(stubContributorInformation),
      publicationInformation = Some(stubPublicationInformation),
      isMicrosite = false,
      trackingInformation = Some(stubTrackingInformation),
      campaignInformation = Some(stubCampaignInformation),
      paidContentInformation = Some(stubPaidContentInformation),
      adBlockingLevel = tAdBlockingLevel,
      contributionBlockingLevel = tContributionBlockingLevel,
      updatedAt = tUpdatedAt,
      keywordType = None,
    )
  }

}
