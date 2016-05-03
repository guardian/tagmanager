package model.command

import model._
import model.command.logic.SponsorshipStatusCalculator
import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Format}
import repositories.SponsorshipOperations._
import repositories.SponsorshipRepository

case class UpdateSponsorshipCommand(
  id: Long,
  validFrom: Option[DateTime],
  validTo: Option[DateTime],
  sponsorshipType: String,
  sponsorName: String,
  sponsorLogo: Image,
  sponsorLink: String,
  tags: Option[List[Long]],
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
      tags = tags,
      section = section,
      targeting = targeting
    )
    val existingSponsorship = SponsorshipRepository.getSponsorship(id)

    for (
      existingSponsorship <- SponsorshipRepository.getSponsorship(id);
      updatedSponsorship <- SponsorshipRepository.updateSponsorship(sponsorship)
    ) yield {

      val existingTags: List[Long] = getActiveTags(existingSponsorship)
      val newTags: List[Long] = getActiveTags(updatedSponsorship)

      val toDelete = existingTags diff newTags
      val toAdd = newTags diff existingTags
      val toReindex = newTags intersect existingTags

      for (tag <- toDelete) removeSponsorshipFromTag(updatedSponsorship.id, tag)
      for (tag <- toAdd) addSponsorshipToTag(updatedSponsorship.id, tag)
      for (tag <- toReindex) reindexTag(tag)

      (getActiveSection(existingSponsorship), getActiveSection(updatedSponsorship)) match {
        case(None, Some(newSectionId)) => addSponsorshipToSection(updatedSponsorship.id, newSectionId)
        case(Some(oldSectionId), None) => removeSponsorshipFromSection(updatedSponsorship.id, oldSectionId)
        case(Some(oldSectionId), Some(newSectionId)) if oldSectionId != newSectionId => {
          removeSponsorshipFromSection(updatedSponsorship.id, oldSectionId)
          addSponsorshipToSection(updatedSponsorship.id, newSectionId)
        }
        case(Some(oldSectionId), Some(newSectionId)) if oldSectionId == newSectionId => reindexSection(newSectionId)
        case _ => // no change
      }

      updatedSponsorship
    }
  }

  private def getActiveTags(s: Sponsorship): List[Long] = {
    s.status match {
      case "active" => s.tags.getOrElse(Nil)
      case _ => Nil
    }
  }

  private def getActiveSection(s: Sponsorship): Option[Long] = {
    s.status match {
      case "active" => s.section
      case _ => None
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
      (JsPath \ "tags").formatNullable[List[Long]] and
      (JsPath \ "section").formatNullable[Long] and
      (JsPath \ "targeting").formatNullable[SponsorshipTargeting]

    )(UpdateSponsorshipCommand.apply, unlift(UpdateSponsorshipCommand.unapply))

}

