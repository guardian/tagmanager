package model.command

import com.gu.tagmanagement.{SectionEvent, EventType, TagEvent}
import model.command.logic.{SponsorshipStatusCalculator, TagPathCalculator}
import model._
import org.apache.commons.lang3.StringUtils
import org.cvogt.play.json.Jsonx
import org.cvogt.play.json.implicits.optionWithNull
import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Format}
import repositories._
import CommandError._
import services.KinesisStreams

case class InlinePaidContentSponsorshipCommand(
                         validFrom: Option[DateTime],
                         validTo: Option[DateTime],
                         sponsorName: String,
                         sponsorLogo: Image,
                         sponsorLink: String,
                         aboutLink: Option[String] = None
                                    ) {

  def createSponsorship(tagId: Long) = {
    val status = SponsorshipStatusCalculator.calculateStatus(validFrom, validTo)

    val sponsorship = Sponsorship(
      id = Sequences.sponsorshipId.getNextId,
      validFrom = validFrom,
      validTo = validTo,
      status = status,
      sponsorshipType = "paidContent",
      sponsorName = sponsorName,
      sponsorLogo = sponsorLogo,
      sponsorLink = sponsorLink,
      aboutLink = aboutLink.flatMap{s => if(StringUtils.isNotBlank(s)) Some(s) else None },
      tags = Some(List(tagId)),
      sections = None,
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
                      sponsorship: Option[InlinePaidContentSponsorshipCommand] = None,
                      paidContentInformation: Option[PaidContentInformation] = None,
                      createMicrosite: Boolean = false
                           ) extends Command {

  type T = Tag

  def process()(implicit username: Option[String] = None): Option[Tag] = {

    val tagId = Sequences.tagId.getNextId

    val sectionId: Option[Long] = if(createMicrosite) {
      val sectionPageId: Long = try { PathManager.registerPathAndGetPageId(slug) } catch { case p: PathRegistrationFailed => PathInUse}

      val nextSectionId = Sequences.sectionId.getNextId

      val createdSection = Section(
        id = nextSectionId,
        sectionTagId = tagId,
        name = externalName,
        path = slug,
        wordsForUrl = slug,
        pageId = sectionPageId,
        editions = Map(),
        discriminator = Some("Navigation"),
        isMicrosite = true,
        activeSponsorships = Nil
      )

      val result = SectionRepository.updateSection(createdSection)

      KinesisStreams.sectionUpdateStream.publishUpdate(createdSection.id.toString, SectionEvent(EventType.Update, createdSection.id, Some(createdSection.asThrift)))

      SectionAuditRepository.upsertSectionAudit(SectionAudit.created(createdSection))

      result.map(_.id)
    } else {
      section
    }

    val calculatedPath = preCalculatedPath match {
      case Some(path) => path
      case None => TagPathCalculator.calculatePath(`type`, slug, sectionId, trackingInformation.map(_.trackingType))
    }

    val pageId = try { PathManager.registerPathAndGetPageId(calculatedPath) } catch { case p: PathRegistrationFailed => PathInUse}

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
      section = sectionId,
      publication = publication,
      categories = categories,
      description = description,
      parents = parents,
      externalReferences = references,
      podcastMetadata = podcastMetadata,
      contributorInformation = contributorInformation,
      publicationInformation = publicationInformation,
      isMicrosite = isMicrosite || createMicrosite,
      capiSectionId = capiSectionId,
      trackingInformation = trackingInformation,
      activeSponsorships = if(createdSponsorshipActive) List(createdSponsorship.map(_.id).get) else Nil,
      sponsorship = createdSponsorship.map(_.id),
      paidContentInformation = paidContentInformation,
      expired = createdSponsorship.map(_.status == "expired").getOrElse(false),
      updatedAt = new DateTime(DateTimeZone.UTC).getMillis
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
