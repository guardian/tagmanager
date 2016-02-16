package model.command

import com.gu.tagmanagement.{SectionEvent, EventType, TagEvent}
import model._
import model.command.logic.SponsorshipStatusCalculator
import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Format}
import repositories._
import services.KinesisStreams

case class CreateSponsorshipCommand(
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
      id = Sequences.sponsorshipId.getNextId,
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

    SponsorshipRepository.updateSponsorship(sponsorship).map { createdSponsorship =>

      if(status == "active") {
        addSponsorshipToTag(createdSponsorship)
        addSponsorshipToSection(createdSponsorship)
      }
      createdSponsorship
    }

  }

  private def addSponsorshipToTag(sponsorship: Sponsorship)(implicit username: Option[String]): Unit = {
    sponsorship.tag.foreach { tagId =>
      TagRepository.getTag(tagId).foreach{ t =>
        val sponsoredTag = t.copy(activeSponsorships = sponsorship.id :: t.activeSponsorships )
        val result = TagRepository.upsertTag(sponsoredTag)

        KinesisStreams.tagUpdateStream.publishUpdate(sponsoredTag.id.toString, TagEvent(EventType.Update, sponsoredTag.id, Some(sponsoredTag.asThrift)))
        TagAuditRepository.upsertTagAudit(TagAudit.updated(sponsoredTag))
      }
    }
  }

  private def addSponsorshipToSection(sponsorship: Sponsorship)(implicit username: Option[String]): Unit = {
    sponsorship.section.foreach { sectionId =>
      SectionRepository.getSection(sectionId).foreach{ s =>
        val sponsoredSection = s.copy(activeSponsorships = sponsorship.id :: s.activeSponsorships )
        val result = SectionRepository.updateSection(sponsoredSection)

        KinesisStreams.sectionUpdateStream.publishUpdate(sponsoredSection.id.toString, SectionEvent(EventType.Update, sponsoredSection.id, Some(sponsoredSection.asThrift)))

        SectionAuditRepository.upsertSectionAudit(SectionAudit.updated(sponsoredSection))
      }
    }
  }
}

object CreateSponsorshipCommand{

  implicit val sponsorshipFormat: Format[CreateSponsorshipCommand] = (
      (JsPath \ "validFrom").formatNullable[DateTime] and
      (JsPath \ "validTo").formatNullable[DateTime] and
      (JsPath \ "sponsorshipType").format[String] and
      (JsPath \ "sponsorName").format[String] and
      (JsPath \ "sponsorLogo").format[Image] and
      (JsPath \ "sponsorLink").format[String] and
      (JsPath \ "tag").formatNullable[Long] and
      (JsPath \ "section").formatNullable[Long] and
      (JsPath \ "targeting").formatNullable[SponsorshipTargeting]

    )(CreateSponsorshipCommand.apply, unlift(CreateSponsorshipCommand.unapply))

}
