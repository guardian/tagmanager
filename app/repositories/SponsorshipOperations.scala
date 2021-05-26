package repositories

import com.gu.tagmanagement.{SectionEvent, EventType, TagEvent}
import model.{SectionAudit, TagAudit}
import services.KinesisStreams
import play.api.Logging
import scala.concurrent.ExecutionContext


object SponsorshipOperations extends Logging {
  def addSponsorshipToTag(sponsorshipId: Long, tagId: Long)(implicit username: Option[String]): Unit = {
    logger.info(s"adding sponsorship $sponsorshipId to tag $tagId")
    TagRepository.getTag(tagId).foreach { t =>
      val sponsoredTag = t.copy(activeSponsorships = (sponsorshipId :: t.activeSponsorships).distinct )
      val result = TagRepository.upsertTag(sponsoredTag)

      KinesisStreams.tagUpdateStream.publishUpdate(sponsoredTag.id.toString, TagEvent(EventType.Update, sponsoredTag.id, Some(sponsoredTag.asThrift)))
      TagAuditRepository.upsertTagAudit(TagAudit.updated(sponsoredTag))
    }
  }

  def removeSponsorshipFromTag(sponsorshipId: Long, tagId: Long)(implicit username: Option[String]): Unit = {
    logger.info(s"removing sponsorship $sponsorshipId from tag $tagId")
    TagRepository.getTag(tagId).foreach { t =>
      val sponsoredTag = t.copy(activeSponsorships = t.activeSponsorships.filterNot(_ == sponsorshipId))
      val result = TagRepository.upsertTag(sponsoredTag)

      KinesisStreams.tagUpdateStream.publishUpdate(sponsoredTag.id.toString, TagEvent(EventType.Update, sponsoredTag.id, Some(sponsoredTag.asThrift)))
      TagAuditRepository.upsertTagAudit(TagAudit.updated(sponsoredTag))
    }
  }

  def reindexTag(tagId: Long)(implicit username: Option[String]): Unit = {
    logger.info(s"reindexing tag $tagId to update sponsorship")
    TagRepository.getTag(tagId).foreach { t =>
      KinesisStreams.tagUpdateStream.publishUpdate(t.id.toString, TagEvent(EventType.Update, t.id, Some(t.asThrift)))
    }
  }

  def addSponsorshipToSection(sponsorshipId: Long, sectionId: Long)(implicit username: Option[String]): Unit = {
    logger.info(s"adding sponsorship $sponsorshipId to section $sectionId")
    SectionRepository.getSection(sectionId).foreach { s =>
      val sponsoredSection = s.copy(activeSponsorships = (sponsorshipId :: s.activeSponsorships).distinct )
      val result = SectionRepository.updateSection(sponsoredSection)

      KinesisStreams.sectionUpdateStream.publishUpdate(sponsoredSection.id.toString, SectionEvent(EventType.Update, sponsoredSection.id, Some(sponsoredSection.asThrift)))

      SectionAuditRepository.upsertSectionAudit(SectionAudit.updated(sponsoredSection))
    }
  }

  def removeSponsorshipFromSection(sponsorshipId: Long, sectionId: Long)(implicit username: Option[String]): Unit = {
    logger.info(s"removing sponsorship $sponsorshipId from section $sectionId")
    SectionRepository.getSection(sectionId).foreach { s =>
      val sponsoredSection = s.copy(activeSponsorships = s.activeSponsorships.filterNot(_ == sponsorshipId) )
      val result = SectionRepository.updateSection(sponsoredSection)

      KinesisStreams.sectionUpdateStream.publishUpdate(sponsoredSection.id.toString, SectionEvent(EventType.Update, sponsoredSection.id, Some(sponsoredSection.asThrift)))

      SectionAuditRepository.upsertSectionAudit(SectionAudit.updated(sponsoredSection))
    }
  }

  def reindexSection(sectionId: Long)(implicit username: Option[String]): Unit = {
    logger.info(s"reindexing section $sectionId  to update sponsorship")
    SectionRepository.getSection(sectionId).foreach { s =>
      KinesisStreams.sectionUpdateStream.publishUpdate(s.id.toString, SectionEvent(EventType.Update, s.id, Some(s.asThrift)))
    }
  }

  def expirePaidContentTag(tagId: Long)(implicit username: Option[String], ec: ExecutionContext): Unit = {
    TagRepository.getTag(tagId).foreach { t =>
      val expiredTag = t.copy(activeSponsorships = Nil, expired = true)
      val result = TagRepository.upsertTag(expiredTag)

      KinesisStreams.tagUpdateStream.publishUpdate(expiredTag.id.toString, TagEvent(EventType.Update, expiredTag.id, Some(expiredTag.asThrift)))
      TagAuditRepository.upsertTagAudit(TagAudit.updated(expiredTag))

      ContentAPI.getContentIdsForTag(t.path) foreach { contentId => KinesisStreams.commercialExpiryStream.publishUpdate(contentId, "true") }
    }
  }

  def unexpirePaidContentTag(tagId: Long)(implicit username: Option[String],ec: ExecutionContext): Unit = {
    println("Unexpiring paid content tag")

    TagRepository.getTag(tagId).foreach { t =>
      println(s"Found tag ${tagId}")

      val unexpiredTag = t.copy(expired = false)
      TagRepository.upsertTag(unexpiredTag)

      TagAuditRepository.upsertTagAudit(TagAudit.updated(unexpiredTag))
      KinesisStreams.tagUpdateStream.publishUpdate(unexpiredTag.id.toString, TagEvent(EventType.Update, unexpiredTag.id, Some(unexpiredTag.asThrift)))
      ContentAPI.getContentIdsForTag(t.path) foreach { contentId => KinesisStreams.commercialExpiryStream.publishUpdate(contentId, "false") }

      println("Done!")
    }
  }
}
