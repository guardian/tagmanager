package model

import com.amazonaws.services.dynamodbv2.document.Item
import play.api.Logger
import play.api.libs.json._
import play.api.libs.functional.syntax._
import org.cvogt.play.json.Jsonx
import org.cvogt.play.json.implicits.optionWithNull
import com.gu.tagmanagement.{Tag => ThriftTag, TagType, TagReindexBatch}
import helpers.XmlHelpers._
import repositories.{SponsorshipRepository, SectionRepository}
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
  activeSponsorships: List[Long] = Nil,
  sponsorship: Option[Long] = None, // for paid content tags, they have an associated sponsorship but it may not be active
  expired: Boolean = false
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
    trackingInformation = trackingInformation.map(_.asThrift)
  )

  // in this limited format for inCopy to consume
  def asExportedXml = {


    val oldType = this.`type` match {
      case "Topic" => "Keyword"
      case t => t
    }

    val section = this.section.map(SectionRepository.getSection(_))
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

object Tag {

  implicit val tagFormat = Jsonx.formatCaseClassUseDefaults[Tag]

  def fromItem(item: Item) = try {
    Json.parse(item.toJSON).as[Tag]
  } catch {
    case NonFatal(e) => {
      Logger.error(s"failed to load tag ${item.toJSON}", e)
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
      trackingInformation = thriftTag.trackingInformation.map(TrackingInformation(_))
    )
}

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
  activeSponsorships: List[Long] = Nil,
  sponsorship: Option[Sponsorship] = None, // for paid content tags, they have an associated sponsorship but it may not be active
  expired: Boolean = false
  )

object DenormalisedTag{

  implicit val tagFormat = Jsonx.formatCaseClassUseDefaults[DenormalisedTag]

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
    activeSponsorships = t.activeSponsorships,
    sponsorship = t.sponsorship.flatMap(SponsorshipRepository.getSponsorship), // for paid content tags, they have an associated sponsorship but it may not be active
    expired = t.expired
  )
}
