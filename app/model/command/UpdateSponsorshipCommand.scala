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
  sections: Option[List[Long]],
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
      sections = sections,
      targeting = targeting
    )
    val existingSponsorship = SponsorshipRepository.getSponsorship(id)

    for (
      existingSponsorship <- SponsorshipRepository.getSponsorship(id);
      updatedSponsorship <- SponsorshipRepository.updateSponsorship(sponsorship)
    ) yield {

      val existingTags: List[Long] = getActiveTags(existingSponsorship)
      val newTags: List[Long] = getActiveTags(updatedSponsorship)

      val tagsToDelete = existingTags diff newTags
      val tagsToAdd = newTags diff existingTags
      val tagsToReindex = newTags intersect existingTags

      for (tag <- tagsToDelete) removeSponsorshipFromTag(updatedSponsorship.id, tag)
      for (tag <- tagsToAdd) addSponsorshipToTag(updatedSponsorship.id, tag)
      for (tag <- tagsToReindex) reindexTag(tag)


      val existingSections: List[Long] = getActiveSections(existingSponsorship)
      val newSections: List[Long] = getActiveSections(updatedSponsorship)

      val sectionsToDelete = existingSections diff newSections
      val sectionsToAdd = newSections diff existingSections
      val sectionsToReindex = newSections intersect existingSections

      for (section <- sectionsToDelete) removeSponsorshipFromSection(updatedSponsorship.id, section)
      for (section <- sectionsToAdd) addSponsorshipToSection(updatedSponsorship.id, section)
      for (section <- sectionsToReindex) reindexSection(section)

      updatedSponsorship
    }
  }

  private def getActiveTags(s: Sponsorship): List[Long] = {
    s.status match {
      case "active" => s.tags.getOrElse(Nil)
      case _ => Nil
    }
  }

  private def getActiveSections(s: Sponsorship): List[Long] = {
    s.status match {
      case "active" => s.sections.getOrElse(Nil)
      case _ => Nil
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
      (JsPath \ "sections").formatNullable[List[Long]] and
      (JsPath \ "targeting").formatNullable[SponsorshipTargeting]

    )(UpdateSponsorshipCommand.apply, unlift(UpdateSponsorshipCommand.unapply))

}

