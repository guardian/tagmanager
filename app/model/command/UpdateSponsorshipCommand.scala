package model.command

import com.gu.tagmanagement.{SectionEvent, EventType, TagEvent}
import model._
import model.command.logic.SponsorshipStatusCalculator
import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Format}
import repositories._
import services.KinesisStreams

case class UpdateSponsorshipCommand(
  id: Long,
  validFrom: Option[DateTime],
  validTo: Option[DateTime],
  sponsorshipType: String,
  sponsorName: String,
  sponsorLogo: Image,
  sponsorLink: String,
  tag: Option[Long],
  section: Option[Long],
  targeting: Option[SponsorshipTargeting]
) extends Command {

  override type T = Sponsorship

  override def process()(implicit username: Option[String]): Option[T] = {

    val status = SponsorshipStatusCalculator.calculateStatus(validFrom, validTo)

    val sponsorship = Sponsorship(
      id = id,
      validFrom = validFrom,
      validTo = validTo,
      status = status,
      sponsorshipType = sponsorshipType,
      sponsorName = sponsorName,
      sponsorLogo = sponsorLogo,
      sponsorLink = sponsorLink,
      tag = tag,
      section = section,
      targeting = targeting
    )
    val existingSponsorship = SponsorshipRepository.getSponsorship(id)

    for (
      existingSponsorship <- SponsorshipRepository.getSponsorship(id);
      updatedSponsorship <- SponsorshipRepository.updateSponsorship(sponsorship)
    ) yield {

      (getActiveTag(existingSponsorship), getActiveTag(updatedSponsorship)) match {
        case(None, Some(newTagId)) => addSponsorshipToTag(updatedSponsorship.id, newTagId)
        case(Some(oldTagId), None) => removeSponsorshipFromTag(updatedSponsorship.id, oldTagId)
        case(Some(oldTagId), Some(newTagId)) if oldTagId != newTagId => {
          removeSponsorshipFromTag(updatedSponsorship.id, oldTagId)
          addSponsorshipToTag(updatedSponsorship.id, newTagId)
        }
        case _ => // no change
      }

      (getActiveSection(existingSponsorship), getActiveSection(updatedSponsorship)) match {
        case(None, Some(newSectionId)) => addSponsorshipToSection(updatedSponsorship.id, newSectionId)
        case(Some(oldSectionId), None) => removeSponsorshipFromSection(updatedSponsorship.id, oldSectionId)
        case(Some(oldSectionId), Some(newSectionId)) if oldSectionId != newSectionId => {
          removeSponsorshipFromSection(updatedSponsorship.id, oldSectionId)
          addSponsorshipToSection(updatedSponsorship.id, newSectionId)
        }
        case _ => // no change
      }

      updatedSponsorship
    }
  }

  private def getActiveTag(s: Sponsorship): Option[Long] = {
    s.status match {
      case "active" => s.tag
      case _ => None
    }
  }

  private def getActiveSection(s: Sponsorship): Option[Long] = {
    s.status match {
      case "active" => s.section
      case _ => None
    }
  }

  private def addSponsorshipToTag(sponsorshipId: Long, tagId: Long)(implicit username: Option[String]): Unit = {

    TagRepository.getTag(tagId).foreach { t =>
      val sponsoredTag = t.copy(activeSponsorships = sponsorshipId :: t.activeSponsorships )
      val result = TagRepository.upsertTag(sponsoredTag)

      KinesisStreams.tagUpdateStream.publishUpdate(sponsoredTag.id.toString, TagEvent(EventType.Update, sponsoredTag.id, Some(sponsoredTag.asThrift)))
      TagAuditRepository.upsertTagAudit(TagAudit.updated(sponsoredTag))
    }
  }

  private def removeSponsorshipFromTag(sponsorshipId: Long, tagId: Long)(implicit username: Option[String]): Unit = {

    TagRepository.getTag(tagId).foreach { t =>
      val sponsoredTag = t.copy(activeSponsorships = t.activeSponsorships.filterNot(_ == tagId))
      val result = TagRepository.upsertTag(sponsoredTag)

      KinesisStreams.tagUpdateStream.publishUpdate(sponsoredTag.id.toString, TagEvent(EventType.Update, sponsoredTag.id, Some(sponsoredTag.asThrift)))
      TagAuditRepository.upsertTagAudit(TagAudit.updated(sponsoredTag))
    }
  }

  private def addSponsorshipToSection(sponsorshipId: Long, sectionId: Long)(implicit username: Option[String]): Unit = {

    SectionRepository.getSection(sectionId).foreach { s =>
      val sponsoredSection = s.copy(activeSponsorships = sponsorshipId :: s.activeSponsorships )
      val result = SectionRepository.updateSection(sponsoredSection)

      KinesisStreams.sectionUpdateStream.publishUpdate(sponsoredSection.id.toString, SectionEvent(EventType.Update, sponsoredSection.id, Some(sponsoredSection.asThrift)))

      SectionAuditRepository.upsertSectionAudit(SectionAudit.updated(sponsoredSection))
    }
  }

  private def removeSponsorshipFromSection(sponsorshipId: Long, sectionId: Long)(implicit username: Option[String]): Unit = {

    SectionRepository.getSection(sectionId).foreach { s =>
      val sponsoredSection = s.copy(activeSponsorships =  s.activeSponsorships.filterNot(_ == sectionId) )
      val result = SectionRepository.updateSection(sponsoredSection)

      KinesisStreams.sectionUpdateStream.publishUpdate(sponsoredSection.id.toString, SectionEvent(EventType.Update, sponsoredSection.id, Some(sponsoredSection.asThrift)))

      SectionAuditRepository.upsertSectionAudit(SectionAudit.updated(sponsoredSection))
    }
  }
}

object UpdateSponsorshipCommand{

  implicit val sponsorshipCommandFormat: Format[UpdateSponsorshipCommand] = (
    (JsPath \ "id").format[Long] and
      (JsPath \ "validFrom").formatNullable[DateTime] and
      (JsPath \ "validTo").formatNullable[DateTime] and
      (JsPath \ "sponsorshipType").format[String] and
      (JsPath \ "sponsorName").format[String] and
      (JsPath \ "sponsorLogo").format[Image] and
      (JsPath \ "sponsorLink").format[String] and
      (JsPath \ "tag").formatNullable[Long] and
      (JsPath \ "section").formatNullable[Long] and
      (JsPath \ "targeting").formatNullable[SponsorshipTargeting]

    )(UpdateSponsorshipCommand.apply, unlift(UpdateSponsorshipCommand.unapply))

}

