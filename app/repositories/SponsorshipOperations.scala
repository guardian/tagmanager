package repositories

import com.gu.tagmanagement.{SectionEvent, EventType, TagEvent}
import model.{SectionAudit, TagAudit}
import services.KinesisStreams
import play.api.Logger


object SponsorshipOperations {
  def addSponsorshipToTag(sponsorshipId: Long, tagId: Long)(implicit username: Option[String]): Unit = {
    Logger.info(s"adding sponsorship $sponsorshipId to tag $tagId")
    TagRepository.getTag(tagId).foreach { t =>
      val sponsoredTag = t.copy(activeSponsorships = sponsorshipId :: t.activeSponsorships )
      val result = TagRepository.upsertTag(sponsoredTag)

      KinesisStreams.tagUpdateStream.publishUpdate(sponsoredTag.id.toString, TagEvent(EventType.Update, sponsoredTag.id, Some(sponsoredTag.asThrift)))
      TagAuditRepository.upsertTagAudit(TagAudit.updated(sponsoredTag))
    }
  }

  def removeSponsorshipFromTag(sponsorshipId: Long, tagId: Long)(implicit username: Option[String]): Unit = {
    Logger.info(s"removing sponsorship $sponsorshipId from tag $tagId")
    TagRepository.getTag(tagId).foreach { t =>
      val sponsoredTag = t.copy(activeSponsorships = t.activeSponsorships.filterNot(_ == sponsorshipId))
      val result = TagRepository.upsertTag(sponsoredTag)

      KinesisStreams.tagUpdateStream.publishUpdate(sponsoredTag.id.toString, TagEvent(EventType.Update, sponsoredTag.id, Some(sponsoredTag.asThrift)))
      TagAuditRepository.upsertTagAudit(TagAudit.updated(sponsoredTag))
    }
  }

  def addSponsorshipToSection(sponsorshipId: Long, sectionId: Long)(implicit username: Option[String]): Unit = {
    Logger.info(s"adding sponsorship $sponsorshipId to section $sectionId")
    SectionRepository.getSection(sectionId).foreach { s =>
      val sponsoredSection = s.copy(activeSponsorships = sponsorshipId :: s.activeSponsorships )
      val result = SectionRepository.updateSection(sponsoredSection)

      KinesisStreams.sectionUpdateStream.publishUpdate(sponsoredSection.id.toString, SectionEvent(EventType.Update, sponsoredSection.id, Some(sponsoredSection.asThrift)))

      SectionAuditRepository.upsertSectionAudit(SectionAudit.updated(sponsoredSection))
    }
  }

  def removeSponsorshipFromSection(sponsorshipId: Long, sectionId: Long)(implicit username: Option[String]): Unit = {
    Logger.info(s"removing sponsorship $sponsorshipId from section $sectionId")
    SectionRepository.getSection(sectionId).foreach { s =>
      val sponsoredSection = s.copy(activeSponsorships =  s.activeSponsorships.filterNot(_ == sponsorshipId) )
      val result = SectionRepository.updateSection(sponsoredSection)

      KinesisStreams.sectionUpdateStream.publishUpdate(sponsoredSection.id.toString, SectionEvent(EventType.Update, sponsoredSection.id, Some(sponsoredSection.asThrift)))

      SectionAuditRepository.upsertSectionAudit(SectionAudit.updated(sponsoredSection))
    }
  }

  def expirePaidContentTag(tagId: Long)(implicit username: Option[String]): Unit = {
    TagRepository.getTag(tagId).foreach { t =>
      val expiredTag = t.copy(activeSponsorships = Nil, expired = true)
      val result = TagRepository.upsertTag(expiredTag)

      KinesisStreams.tagUpdateStream.publishUpdate(expiredTag.id.toString, TagEvent(EventType.Update, expiredTag.id, Some(expiredTag.asThrift)))
      TagAuditRepository.upsertTagAudit(TagAudit.updated(expiredTag))

      ContentAPI.getContentIdsForTag(t.path) foreach { contentId => KinesisStreams.commercialExpiryStream.publishUpdate(contentId, "true") }
    }
  }
}
