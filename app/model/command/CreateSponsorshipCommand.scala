package model.command

import com.gu.tagmanagement.{SectionEvent, EventType, TagEvent}
import model._
import model.command.logic.SponsorshipStatusCalculator
import org.apache.commons.lang3.StringUtils
import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Format}
import repositories.SponsorshipOperations._
import repositories.{SponsorshipRepository, Sequences}
import services.KinesisStreams

case class CreateSponsorshipCommand(
  validFrom: Option[DateTime],
  validTo: Option[DateTime],
  sponsorshipType: String,
  sponsorName: String,
  sponsorLogo: Image,
  sponsorLink: String,
  aboutLink: Option[String],
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
      aboutLink = aboutLink.flatMap{s => if(StringUtils.isNotBlank(s)) Some(s) else None },
      tag = tag,
      section = section,
      targeting = targeting
    )

    SponsorshipRepository.updateSponsorship(sponsorship).map { createdSponsorship =>

      if(status == "active") {
        createdSponsorship.tag foreach {tagId => addSponsorshipToTag(createdSponsorship.id, tagId)}
        createdSponsorship.section foreach {sectionId => addSponsorshipToSection(createdSponsorship.id, sectionId)}
      }
      createdSponsorship
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
      (JsPath \ "aboutLink").formatNullable[String] and
      (JsPath \ "tag").formatNullable[Long] and
      (JsPath \ "section").formatNullable[Long] and
      (JsPath \ "targeting").formatNullable[SponsorshipTargeting]

    )(CreateSponsorshipCommand.apply, unlift(CreateSponsorshipCommand.unapply))

}
