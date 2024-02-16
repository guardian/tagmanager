package model.command

import model._
import model.command.logic.SponsorshipStatusCalculator
import org.apache.commons.lang3.StringUtils
import org.joda.time.DateTime
import helpers.JodaDateTimeFormat._
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}
import repositories.SponsorshipOperations._
import repositories.{Sequences, SponsorshipRepository}

import scala.concurrent.{Future, ExecutionContext}

case class CreateSponsorshipCommand(
  validFrom: Option[DateTime],
  validTo: Option[DateTime],
  sponsorshipType: String,
  sponsorshipPackage: Option[String],
  sponsorName: String,
  sponsorLogo: Image,
  highContrastSponsorLogo: Option[Image],
  sponsorLink: String,
  aboutLink: Option[String],
  tags: Option[List[Long]],
  sections: Option[List[Long]],
  targeting: Option[SponsorshipTargeting]
) extends Command {

  override type T = Sponsorship

  override def process()(implicit username: Option[String], ec: ExecutionContext): Future[Option[T]] = Future{

    val status = SponsorshipStatusCalculator.calculateStatus(validFrom, validTo)

    val sponsorship = Sponsorship(
      id = Sequences.sponsorshipId.getNextId,
      validFrom = validFrom,
      validTo = validTo,
      status = status,
      sponsorshipType = sponsorshipType,
      sponsorshipPackage = sponsorshipPackage,
      sponsorName = sponsorName,
      sponsorLogo = sponsorLogo,
      highContrastSponsorLogo = highContrastSponsorLogo,
      sponsorLink = sponsorLink,
      aboutLink = aboutLink.flatMap{s => if(StringUtils.isNotBlank(s)) Some(s) else None },
      tags = tags,
      sections = sections,
      targeting = targeting
    )

    SponsorshipRepository.updateSponsorship(sponsorship).map { createdSponsorship =>

      if(status == "active") {
        for(
          tags <- createdSponsorship.tags;
          tagId <- tags
        ) {
          addSponsorshipToTag(createdSponsorship.id, tagId)
        }
        for(
          sections <- createdSponsorship.sections;
          sectionId <- sections
        ) {
          addSponsorshipToSection(createdSponsorship.id, sectionId)
        }
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
      (JsPath \ "sponsorshipPackage").formatNullable[String] and
      (JsPath \ "sponsorName").format[String] and
      (JsPath \ "sponsorLogo").format[Image] and
      (JsPath \ "highContrastSponsorLogo").formatNullable[Image] and
      (JsPath \ "sponsorLink").format[String] and
      (JsPath \ "aboutLink").formatNullable[String] and
      (JsPath \ "tags").formatNullable[List[Long]] and
      (JsPath \ "sections").formatNullable[List[Long]] and
      (JsPath \ "targeting").formatNullable[SponsorshipTargeting]

    )(CreateSponsorshipCommand.apply, unlift(CreateSponsorshipCommand.unapply))

}
