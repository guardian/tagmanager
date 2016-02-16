package model.command

import com.gu.tagmanagement.{EventType, TagEvent}
import model.command.logic.TagPathCalculator
import model._
import org.cvogt.play.json.Jsonx
import org.cvogt.play.json.implicits.optionWithNull
import org.joda.time.DateTime
import play.api.libs.json.{JsPath, Format}
import repositories._
import CommandError._
import services.KinesisStreams

case class InlinePaidContentSponsorshipCommand(
                         validFrom: Option[DateTime],
                         validTo: Option[DateTime],
                         sponsorName: String,
                         sponsorLogo: Image,
                         sponsorLink: String
                                    ) {

  def createSponsorship(tagId: Long) = {
    val status = (validFrom, validTo) match {
      case(None, None)                              => "active"
      case(Some(from), None) if from.isBeforeNow    => "active"
      case(Some(from), None)                        => "pending"
      case(None, Some(to)) if to.isBeforeNow        => "expired"
      case(None, Some(to))                          => "active"
      case(Some(from), Some(to)) if from.isAfterNow => "pending"
      case(Some(from), Some(to)) if to.isBeforeNow  => "expired"
      case(_)                                       => "active"
    }

    val sponsorship = Sponsorship(
      id = Sequences.sponsorshipId.getNextId,
      validFrom = validFrom,
      validTo = validTo,
      status = status,
      sponsorshipType = "paidContent",
      sponsorName = sponsorName,
      sponsorLogo = sponsorLogo,
      sponsorLink = sponsorLink,
      tag = Some(tagId),
      section = None,
      targeting = None
    )

    SponsorshipRepository.updateSponsorship(sponsorship)
  }
}

object InlinePaidContentSponsorshipCommand {
  implicit val inlinePaidContentSponsorshipFormat = Jsonx.formatCaseClassUseDefaults[InlinePaidContentSponsorshipCommand]
}

case class CreateTagCommand(
                      `type`: String,
                      internalName: String,
                      externalName: String,
                      slug: String,
                      hidden: Boolean = false,
                      legallySensitive: Boolean = false,
                      comparableValue: String,
                      categories: Set[String] = Set(),
                      section: Option[Long] = None,
                      publication: Option[Long] = None,
                      description: Option[String] = None,
                      parents: Set[Long] = Set(),
                      references: List[Reference] = Nil,
                      podcastMetadata: Option[PodcastMetadata] = None,
                      contributorInformation: Option[ContributorInformation] = None,
                      publicationInformation: Option[PublicationInformation] = None,
                      isMicrosite: Boolean,
                      capiSectionId: Option[String] = None,
                      trackingInformation: Option[TrackingInformation] = None,
                      preCalculatedPath: Option[String] = None, //This is used so path isn't calculated
                      sponsorship: Option[InlinePaidContentSponsorshipCommand] = None
                           ) extends Command {

  type T = Tag

  def process()(implicit username: Option[String] = None): Option[Tag] = {

    val calculatedPath = preCalculatedPath match {
      case Some(path) => path
      case None => TagPathCalculator.calculatePath(`type`, slug, section, trackingInformation.map(_.trackingType))
    }

    val pageId = try { PathManager.registerPathAndGetPageId(calculatedPath) } catch { case p: PathRegistrationFailed => PathInUse}

    val tagId = Sequences.tagId.getNextId

    val createdSponsorship = sponsorship flatMap(_.createSponsorship(tagId))
    val createdSponsorshipActive = createdSponsorship.map(_.status == "active").getOrElse(false)

    val tag = Tag(
      id = tagId,
      path = calculatedPath,
      pageId = pageId,
      `type`= `type`,
      internalName = internalName,
      externalName = externalName,
      slug = slug,
      hidden = hidden,
      legallySensitive = legallySensitive,
      comparableValue = comparableValue,
      section = section,
      publication = publication,
      categories = categories,
      description = description,
      parents = parents,
      externalReferences = references,
      podcastMetadata = podcastMetadata,
      contributorInformation = contributorInformation,
      publicationInformation = publicationInformation,
      isMicrosite = isMicrosite,
      capiSectionId = capiSectionId,
      trackingInformation = trackingInformation,
      activeSponsorships = if(createdSponsorshipActive) List(createdSponsorship.map(_.id).get) else Nil,
      sponsorship = createdSponsorship.map(_.id),
      expired = createdSponsorship.map(_.status == "expired").getOrElse(false)
    )
    
    val result = TagRepository.upsertTag(tag)

    KinesisStreams.tagUpdateStream.publishUpdate(tag.id.toString, TagEvent(EventType.Update, tag.id, Some(tag.asThrift)))

    TagAuditRepository.upsertTagAudit(TagAudit.created(tag))

    result
  }
}

object CreateTagCommand {

  implicit val createTagCommandFormat = Jsonx.formatCaseClassUseDefaults[CreateTagCommand]
}
