package model.command

import com.gu.tagmanagement.{EventType, SectionEvent, TagEvent}
import model.command.logic.{SponsorshipStatusCalculator, TagPathCalculator}
import model._
import org.apache.commons.lang3.StringUtils
import ai.x.play.json.Jsonx
import ai.x.play.json.Encoders.encoder
import ai.x.play.json.implicits.optionWithNull
import play.api.libs.json.JodaWrites._
import play.api.libs.json.JodaReads._
import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}
import repositories._
import CommandError._
import play.api.Logging
import services.{Contexts, KinesisStreams}

import scala.concurrent.Future

case class InlinePaidContentSponsorshipCommand(
                         validFrom: Option[DateTime],
                         validTo: Option[DateTime],
                         sponsorName: String,
                         sponsorLogo: Image,
                         highContrastSponsorLogo: Option[Image] = None,
                         sponsorLink: String,
                         aboutLink: Option[String] = None
                                    ) {

  def createSponsorship(tagId: Long, createdSectionId: Option[Long]) = {
    val status = SponsorshipStatusCalculator.calculateStatus(validFrom, validTo)

    val sponsorship = Sponsorship(
      id = Sequences.sponsorshipId.getNextId,
      validFrom = validFrom,
      validTo = validTo,
      status = status,
      sponsorshipType = "paidContent",
      sponsorName = sponsorName,
      sponsorLogo = sponsorLogo,
      highContrastSponsorLogo = highContrastSponsorLogo,
      sponsorLink = sponsorLink,
      aboutLink = aboutLink.flatMap{s => if(StringUtils.isNotBlank(s)) Some(s) else None },
      tags = Some(List(tagId)),
      sections = createdSectionId.map(List(_)),
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
                      campaignInformation: Option[CampaignInformation] = None,
                      preCalculatedPath: Option[String] = None, //This is used so path isn't calculated
                      sponsorship: Option[InlinePaidContentSponsorshipCommand] = None,
                      paidContentInformation: Option[PaidContentInformation] = None,
                      createMicrosite: Boolean = false,
                      adBlockingLevel: Option[BlockingLevel] = None,
                      contributionBlockingLevel: Option[BlockingLevel] = None
                           ) extends Command with Logging {

  type T = Tag

  def process()(implicit username: Option[String] = None): Future[Option[Tag]] = Future {

    val tagId = Sequences.tagId.getNextId

    logger.info(s"Create Tag command process started for tagid: $tagId")

    val sectionId: Option[Long] = if(createMicrosite) {

      val path = if(`type` == "PaidContent" && paidContentInformation.isDefined && paidContentInformation.get.paidContentType == "HostedContent") {
        s"advertiser-content/$slug"
      } else {slug}

      val sectionPageId: Long = try { PathManager.registerPathAndGetPageId(path) } catch { case p: PathRegistrationFailed => PathInUse}

      val nextSectionId = Sequences.sectionId.getNextId

      val createdSection = Section(
        id = nextSectionId,
        sectionTagId = tagId,
        name = externalName,
        path = path,
        wordsForUrl = path,
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

    val createdSectionId = if(createMicrosite) { sectionId } else { None }

    val tagSubType: Option[String] =  `type` match {
      case "Tracking" => trackingInformation.map(_.trackingType)
      case "Campaign" => campaignInformation.map(_.campaignType)
      case _ => None
    }

    val calculatedPath = preCalculatedPath match {
      case Some(path) => path
      case None => TagPathCalculator.calculatePath(`type`, slug, sectionId, tagSubType)
        // Note we don't pass the paid contnet subtype here as the path munging has now been applied to the created microsite
        // so the standard section / slug logic is the desired logic
    }

    val pageId = try { PathManager.registerPathAndGetPageId(calculatedPath) } catch { case p: PathRegistrationFailed => PathInUse}

    val createdSponsorship = sponsorship flatMap(_.createSponsorship(tagId, createdSectionId))
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
      campaignInformation = campaignInformation,
      activeSponsorships = if(createdSponsorshipActive) List(createdSponsorship.map(_.id).get) else Nil,
      sponsorship = createdSponsorship.map(_.id),
      paidContentInformation = paidContentInformation,
      expired = createdSponsorship.map(_.status == "expired").getOrElse(false),
      updatedAt = new DateTime(DateTimeZone.UTC).getMillis,
      adBlockingLevel = adBlockingLevel,
      contributionBlockingLevel = contributionBlockingLevel
    )

    val result = TagRepository.upsertTag(tag)

    val thriftTag = tag.asThrift

    val tagUpdateEvent = TagEvent(EventType.Update, tag.id, Some(thriftTag))

    logger.info(s"Kiniesis producer publish tag-update event, tagEvent type: ${tagUpdateEvent.eventType}")

    KinesisStreams.tagUpdateStream.publishUpdate(tag.id.toString, tagUpdateEvent)

    TagAuditRepository.upsertTagAudit(TagAudit.created(tag))

    if(createdSponsorshipActive) {
      for (
        sectionId <- createdSectionId;
        sponsorship <- createdSponsorship
      ) {
        SponsorshipOperations.addSponsorshipToSection(sponsorship.id, sectionId)
      }
    }

    result
  }(Contexts.tagOperationContext)
}

object CreateTagCommand {

  implicit val createTagCommandFormat = Jsonx.formatCaseClassUseDefaults[CreateTagCommand]
}
