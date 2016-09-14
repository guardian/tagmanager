package services.migration

import com.gu.tagmanagement.{EventType, TagEvent}
import model.command.FlexTagReindexCommand
import model.command.logic.SponsorshipStatusCalculator
import model.{TagAudit, PaidContentInformation, Sponsorship}
import play.api.Logger
import repositories.{SectionRepository, TagAuditRepository, SponsorshipRepository, TagRepository}
import services.KinesisStreams

class AbortItemMigrationException extends RuntimeException

object PaidContentMigrator {

  // NB sponsorships will arrive with ids but the status will not have been set. They are not persisted at this point.
  def migrate(sponsorship: Sponsorship): Unit = {
    implicit val username: Option[String] = Some("PaidContent Migration")
    val tagIds = sponsorship.tags.getOrElse(Nil) ::: sponsorship.sections.getOrElse(Nil).flatMap(SectionRepository.getSection(_).map(_.sectionTagId))
    val tags = tagIds.flatMap(TagRepository.getTag(_))

    tags foreach { t =>
      try {
        Logger.info(s"migrating tag ${t.internalName} to paid content type")

//        if (sponsorship.sponsorshipType != "paidContent") {
//          Logger.info(s"sponsorship provided is not a paid content type, aborting")
//          throw new AbortItemMigrationException
//        }

        val status = SponsorshipStatusCalculator.calculateStatus(sponsorship.validFrom, sponsorship.validTo)
        val sponsorshipWithStatus = sponsorship.copy(
          status = status,
          tags = Some(tagIds),
          sections = None,
          sponsorshipType = "paidContent"
        )

        val paidContentTag = t.copy(
          `type` = "PaidContent",
          paidContentInformation = Some(PaidContentInformation(paidContentType = t.`type`)),
          activeSponsorships = if (status == "active") List(sponsorship.id) else Nil,
          expired = status == "expired",
          sponsorship = Some(sponsorship.id)
        )

        SponsorshipRepository.updateSponsorship(sponsorshipWithStatus)
        TagRepository.upsertTag(paidContentTag) foreach { updatedTag =>
          KinesisStreams.tagUpdateStream.publishUpdate(updatedTag.id.toString, TagEvent(EventType.Update, updatedTag.id, Some(updatedTag.asThrift)))
          TagAuditRepository.upsertTagAudit(TagAudit.updated(updatedTag))
          FlexTagReindexCommand(updatedTag).process()
        }
      } catch {
        case abort: AbortItemMigrationException => // swallow and migrate the next item
      }
    }
  }
}
