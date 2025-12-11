package model

import com.amazonaws.services.dynamodbv2.document.Item
import play.api.Logging
import play.api.libs.json._
import ai.x.play.json.Encoders.encoder
import ai.x.play.json.Jsonx
import com.gu.tagmanagement.{TagReindexBatch, TagType, BlockingLevel => ThriftAdBlockingLevel, Tag => ThriftTag}
import helpers.XmlHelpers._
import repositories.{SectionRepository, SponsorshipRepository}

import scala.util.control.NonFatal
import scala.xml.Node

case class Tag(
                id: Long,
                path: String,
                pageId: Long,
                `type`: String,
                internalName: String,
                externalName: String,
                slug: String,
                hidden: Boolean = false,
                legallySensitive: Boolean = false,
                comparableValue: String,
                categories: Set[String] = Set(),
                section: Option[Long],
                publication: Option[Long],
                description: Option[String] = None,
                parents: Set[Long] = Set(),
                externalReferences: List[Reference] = Nil,
                podcastMetadata: Option[PodcastMetadata] = None,
                contributorInformation: Option[ContributorInformation] = None,
                publicationInformation: Option[PublicationInformation] = None,
                isMicrosite: Boolean,
                capiSectionId: Option[String] = None,
                trackingInformation: Option[TrackingInformation],
                campaignInformation: Option[CampaignInformation],
                activeSponsorships: List[Long] = Nil,
                sponsorship: Option[Long] = None, // for paid content tags, they have an associated sponsorship but it may not be active
                paidContentInformation: Option[PaidContentInformation] = None,
                expired: Boolean = false,
                adBlockingLevel: Option[BlockingLevel],
                contributionBlockingLevel: Option[BlockingLevel],
                var updatedAt: Long = 0L
) {

  def toItem = Item.fromJSON(Json.toJson(this).toString())

  def asThrift = ThriftTag(
    id                = id,
    path              = path,
    pageId            = pageId,
    `type`            = TagType.valueOf(`type`).get,
    internalName      = internalName,
    externalName      = externalName,
    slug              = slug,
    hidden            = hidden,
    legallySensitive  = legallySensitive,
    comparableValue   = comparableValue,
    section           = section,
    publication       = publication,
    description       = description,
    parents           = parents,
    references        = externalReferences.map(_.asThrift),
    podcastMetadata   = podcastMetadata.map(_.asThrift),
    contributorInformation = contributorInformation.map(_.asThrift),
    publicationInformation = publicationInformation.map(_.asThrift),
    isMicrosite       = isMicrosite,
    capiSectionId     = capiSectionId,
    trackingInformation = trackingInformation.map(_.asThrift),
    updatedAt = Some(updatedAt),
    // TODO should this line be flatMap? if there is an active sponsorship ID here, then that sponsorship should
    // exist in the repository. If it doesn't, unexpected behaviour can & will happen downstream!!
    activeSponsorships = if (activeSponsorships.isEmpty) None else Some(activeSponsorships.flatMap {sid =>
      SponsorshipRepository.getSponsorship(sid).map(_.asThrift)
    }),
    sponsorshipId = sponsorship,
    paidContentInformation = paidContentInformation.map(_.asThrift),
    expired = expired,
    campaignInformation = campaignInformation.map(_.asThrift),
    adBlockingLevel = adBlockingLevel.flatMap(level => ThriftAdBlockingLevel.valueOf(level.entryName)),
    contributionBlockingLevel = contributionBlockingLevel.flatMap(level => ThriftAdBlockingLevel.valueOf(level.entryName))
  )

  // in this limited format for inCopy to consume
  def asExportedXml(sectionCache: Map[Long, Section]) = {


    val oldType = this.`type` match {
      case "Topic" => "Keyword"
      case "NewspaperBook" => "Newspaper Book"
      case "NewspaperBookSection" => "Newspaper Book Section"
      case t => t
    }

    val section = this.section.map(sectionCache.get(_))
    val el = createElem("tag")
    val id = createAttribute("id", Some(this.id))
    val externalName = createAttribute("externalname", Some(this.externalName))
    val internalName = createAttribute("internalname", Some(this.internalName))
    val urlWords = createAttribute("words-for-url", Some(this.slug))
    val sectionId = createAttribute("section-id", Some(this.section.getOrElse(281))) //R2 Global Id
    val sectionName = createAttribute("section", Some(section.map(_.map(_.name).getOrElse(None)).getOrElse("Global")))
    val sectionUrl = createAttribute("section-words-for-url", Some(section.map(_.map(_.wordsForUrl).getOrElse(None)).getOrElse("global")))
    val `type` = createAttribute("type", Some(oldType))
    val cmsPrefix = createAttribute("section-cms-path-prefix", Some("/Guardian/" + section.map(_.map(_.path).getOrElse("")).getOrElse("global")))

    val withAttrs = el % id % externalName % internalName % urlWords % sectionId % sectionName % sectionUrl % `type` % cmsPrefix

    val withRefs: Node = this.externalReferences.foldLeft(withAttrs: Node) { (x, y) =>
      addChild(x, y.asExportedXml)
    }
    val withParents: Node = this.parents.foldLeft(withRefs: Node) { (x, parent) =>
      val el = createElem("parent") % createAttribute("id",
        Some(parent))
      addChild(x, el)
    }
    withParents
  }
}

object Tag extends Logging {

  implicit val tagFormat: Format[Tag] = Jsonx.formatCaseClassUseDefaults[Tag]

  def fromItem(item: Item) = try {
    Json.parse(item.toJSON).as[Tag]
  } catch {
    case NonFatal(e) => {
      logger.error(s"failed to load tag ${item.toJSON}", e)
      throw e
    }
  }

  def createReindexBatch(toBatch: List[Tag]): TagReindexBatch = {
    TagReindexBatch(
      tags = toBatch.map(_.asThrift)
    )
  }

  def fromJson(json: JsValue) = json.as[Tag]

  def apply(thriftTag: ThriftTag): Tag =
    Tag(
      id                = thriftTag.id,
      path              = thriftTag.path,
      pageId            = thriftTag.pageId,
      `type`            = thriftTag.`type`.name,
      internalName      = thriftTag.internalName,
      externalName      = thriftTag.externalName,
      slug              = thriftTag.slug,
      hidden            = thriftTag.hidden,
      legallySensitive  = thriftTag.legallySensitive,
      comparableValue   = thriftTag.comparableValue,
      section           = thriftTag.section,
      publication       = thriftTag.publication,
      description       = thriftTag.description,
      parents           = thriftTag.parents.toSet,
      externalReferences        = thriftTag.references.map(Reference(_)).toList,
      podcastMetadata   = thriftTag.podcastMetadata.map(PodcastMetadata(_)),
      contributorInformation = thriftTag.contributorInformation.map(ContributorInformation(_)),
      publicationInformation = thriftTag.publicationInformation.map(PublicationInformation(_)),
      isMicrosite       = thriftTag.isMicrosite,
      capiSectionId     = thriftTag.capiSectionId,
      trackingInformation = thriftTag.trackingInformation.map(TrackingInformation(_)),
      campaignInformation = thriftTag.campaignInformation.map(CampaignInformation(_)),
      updatedAt = thriftTag.updatedAt.getOrElse(0L),
      activeSponsorships = thriftTag.activeSponsorships.map(_.map(_.id).toList).getOrElse(Nil),
      sponsorship = thriftTag.sponsorshipId,
      paidContentInformation = thriftTag.paidContentInformation.map(PaidContentInformation(_)),
      expired = thriftTag.expired,
      adBlockingLevel =  thriftTag.adBlockingLevel.map(tLevel => BlockingLevel.withName(tLevel.name)),
      contributionBlockingLevel =  thriftTag.contributionBlockingLevel.map(tLevel => BlockingLevel.withName(tLevel.name))
    )
}

// The difference between a denormalised tag and a regular tag is the sponsorship object is copied in for the
// denormalised version and the "normalised" version has it stored as a Long id
case class DenormalisedTag (
  id: Long,
  path: String,
  pageId: Long,
  `type`: String,
  internalName: String,
  externalName: String,
  slug: String,
  hidden: Boolean = false,
  legallySensitive: Boolean = false,
  comparableValue: String,
  categories: Set[String] = Set(),
  section: Option[Long],
  publication: Option[Long],
  description: Option[String] = None,
  parents: Set[Long] = Set(),
  externalReferences: List[Reference] = Nil,
  podcastMetadata: Option[PodcastMetadata] = None,
  contributorInformation: Option[ContributorInformation] = None,
  publicationInformation: Option[PublicationInformation] = None,
  isMicrosite: Boolean,
  capiSectionId: Option[String] = None,
  trackingInformation: Option[TrackingInformation],
  campaignInformation: Option[CampaignInformation],
  activeSponsorships: List[Long] = Nil,
  sponsorship: Option[Sponsorship] = None, // for paid content tags, they have an associated sponsorship but it may not be active
  paidContentInformation: Option[PaidContentInformation] = None,
  expired: Boolean = false,
  adBlockingLevel: Option[BlockingLevel],
  contributionBlockingLevel: Option[BlockingLevel]
  ) {

  def normalise(): (Tag, Option[Sponsorship]) = {

    val tag = Tag(
      id = id,
      path = path,
      pageId = pageId,
      `type` = `type`,
      internalName = internalName,
      externalName = externalName,
      slug = slug,
      hidden = hidden,
      legallySensitive = legallySensitive,
      comparableValue = comparableValue,
      categories = categories,
      section = section,
      publication = publication,
      description = description,
      parents = parents,
      externalReferences = externalReferences,
      podcastMetadata = podcastMetadata,
      contributorInformation = contributorInformation,
      publicationInformation = publicationInformation,
      isMicrosite = isMicrosite,
      capiSectionId = capiSectionId,
      trackingInformation = trackingInformation,
      campaignInformation = campaignInformation,
      activeSponsorships = activeSponsorships,
      sponsorship = sponsorship.map(_.id), // for paid content tags, they have an associated sponsorship but it may not be active
      paidContentInformation = paidContentInformation,
      expired = expired,
      adBlockingLevel = adBlockingLevel,
      contributionBlockingLevel = contributionBlockingLevel
    )
    (tag, sponsorship)
  }
}

object DenormalisedTag{

  implicit val tagFormat: OFormat[DenormalisedTag] = Jsonx.formatCaseClassUseDefaults[DenormalisedTag]

  def apply(t: Tag): DenormalisedTag = DenormalisedTag(
    id = t.id,
    path = t.path,
    pageId = t.pageId,
    `type` = t.`type`,
    internalName = t.internalName,
    externalName = t.externalName,
    slug = t.slug,
    hidden = t.hidden,
    legallySensitive = t.legallySensitive,
    comparableValue = t.comparableValue,
    categories = t.categories,
    section = t.section,
    publication = t.publication,
    description = t.description,
    parents = t.parents,
    externalReferences = t.externalReferences,
    podcastMetadata = t.podcastMetadata,
    contributorInformation = t.contributorInformation,
    publicationInformation = t.publicationInformation,
    isMicrosite = t.isMicrosite,
    capiSectionId = t.capiSectionId,
    trackingInformation = t.trackingInformation,
    campaignInformation = t.campaignInformation,
    activeSponsorships = t.activeSponsorships,
    sponsorship = t.sponsorship.flatMap(SponsorshipRepository.getSponsorship), // for paid content tags, they have an associated sponsorship but it may not be active
    paidContentInformation = t.paidContentInformation,
    expired = t.expired,
    adBlockingLevel = t.adBlockingLevel,
    contributionBlockingLevel = t.contributionBlockingLevel
  )
}
