package model

import com.amazonaws.services.dynamodbv2.document.Item
import play.api.Logger
import play.api.libs.json._
import play.api.libs.functional.syntax._
import com.gu.tagmanagement.{Tag => ThriftTag, TagType}

import scala.util.control.NonFatal

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
  publicationInformation: Option[PublicationInformation] = None
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
    publicationInformation = publicationInformation.map(_.asThrift)
  )

  def asXml = {
      <tag>
        <id>{this.id}</id>
        <path>{this.path}</path>
        <pageId>{this.pageId}</pageId>
        <type>{this.`type`}</type>
        <internalName>{this.internalName}</internalName>
        <externalName>{this.externalName}</externalName>
        <slug>{this.slug}</slug>
        <hidden>{this.hidden}</hidden>
        <legallySensitive>{this.legallySensitive}</legallySensitive>
        <comparableValue>{this.comparableValue}</comparableValue>
        <section>{this.section.getOrElse("")}</section>
        <publication>{this.publication.getOrElse("")}</publication>
        <description>{this.description.getOrElse("")}</description>
        <parents>{this.parents}</parents>
        <references>{this.references.map(_.asXml)}</references>
        <podcastMetadata>{this.podcastMetadata.map(_.asXml).getOrElse("")}</podcastMetadata>
        <contributorInformation>{this.contributorInformation.map(_.asXml).getOrElse("")}</contributorInformation>
        <publicationInformation>{this.publicationInformation.map(_.asXml).getOrElse("")}</publicationInformation>
      </tag>
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
      (JsPath \ "publicationInformation").formatNullable[PublicationInformation]
    )(Tag.apply, unlift(Tag.unapply))

  def fromItem(item: Item) = try {
    Json.parse(item.toJSON).as[Tag]
  } catch {
    case NonFatal(e) => {
      Logger.error(s"failed to load tag ${item.toJSON}", e)
      throw e
    }
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
      publicationInformation = thriftTag.publicationInformation.map(PublicationInformation(_))
    )
}
