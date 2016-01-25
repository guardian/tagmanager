package model

import com.amazonaws.services.dynamodbv2.document.Item
import play.api.Logger
import play.api.libs.json._
import play.api.libs.functional.syntax._
import com.gu.tagmanagement.{Tag => ThriftTag, TagType, TagReindexBatch}
import helpers.XmlHelpers._
import repositories.SectionRepository
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
  references: List[Reference] = Nil,
  podcastMetadata: Option[PodcastMetadata] = None,
  contributorInformation: Option[ContributorInformation] = None,
  publicationInformation: Option[PublicationInformation] = None,
  isMicrosite: Boolean
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
    references        = references.map(_.asThrift),
    podcastMetadata   = podcastMetadata.map(_.asThrift),
    contributorInformation = contributorInformation.map(_.asThrift),
    publicationInformation = publicationInformation.map(_.asThrift),
    isMicrosite       = isMicrosite
  )

  // in this limited format for inCopy to consume
  def asExportedXml = {
    val el = createElem("tag")
    val section = SectionRepository.getSection(this.id)
    val id = createAttribute("id", Some(this.id))
    val externalName = createAttribute("externalname", Some(this.externalName))
    val internalName = createAttribute("internalname", Some(this.internalName))
    val urlWords = createAttribute("words-for-url", Some(this.slug))
    val sectionId = createAttribute("section-id", this.section)
    val sectionName = createAttribute("section", section.map(_.name))
    val sectionUrl = createAttribute("section-words-for-url", section.map(_.wordsForUrl))
    val `type` = createAttribute("type", Some(this.`type`))

    val withAttrs = el % id % externalName % internalName % urlWords % sectionId % sectionName % sectionUrl % `type`

    val withRefs: Node = this.references.foldLeft(withAttrs: Node) { (x, y) =>
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

  implicit val tagFormat: Format[Tag] = (
      (JsPath \ "id").format[Long] and
      (JsPath \ "path").format[String] and
      (JsPath \ "pageId").format[Long] and
      (JsPath \ "type").format[String] and
      (JsPath \ "internalName").format[String] and
      (JsPath \ "externalName").format[String] and
      (JsPath \ "slug").format[String] and
      (JsPath \ "hidden").format[Boolean] and
      (JsPath \ "legallySensitive").format[Boolean] and
      (JsPath \ "comparableValue").format[String] and
      (JsPath \ "categories").formatNullable[Set[String]].inmap[Set[String]](_.getOrElse(Set()), Some(_)) and
      (JsPath \ "section").formatNullable[Long] and
      (JsPath \ "publication").formatNullable[Long] and
      (JsPath \ "description").formatNullable[String] and
      (JsPath \ "parents").formatNullable[Set[Long]].inmap[Set[Long]](_.getOrElse(Set()), Some(_)) and
      (JsPath \ "externalReferences").formatNullable[List[Reference]].inmap[List[Reference]](_.getOrElse(Nil), Some(_)) and
      (JsPath \ "podcastMetadata").formatNullable[PodcastMetadata] and
      (JsPath \ "contributorInformation").formatNullable[ContributorInformation] and
      (JsPath \ "publicationInformation").formatNullable[PublicationInformation] and
      (JsPath \ "isMicrosite").format[Boolean]

    )(Tag.apply, unlift(Tag.unapply))

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
      references        = thriftTag.references.map(Reference(_)).toList,
      podcastMetadata   = thriftTag.podcastMetadata.map(PodcastMetadata(_)),
      contributorInformation = thriftTag.contributorInformation.map(ContributorInformation(_)),
      publicationInformation = thriftTag.publicationInformation.map(PublicationInformation(_)),
      isMicrosite       = thriftTag.isMicrosite
    )
}
