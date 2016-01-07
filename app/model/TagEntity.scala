package model
import play.api.libs.json._
import repositories.SectionRepository

//used for responses in the hypermedia api

case class TagEntity(
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
  section: Option[Section],
  description: Option[String] = None,
  podcastMetadata: Option[PodcastMetadata] = None,
  contributorInformation: Option[ContributorInformation] = None,
  parents: Set[EmbeddedEntity[TagEntity]] = Set(),
  references: List[Reference] = Nil
)

object TagEntity {
  def apply(tag: Tag): TagEntity = {
      TagEntity(
        tag.id,
        tag.path,
        tag.pageId,
        tag.`type`,
        tag.internalName,
        tag.externalName,
        tag.slug,
        tag.hidden,
        tag.legallySensitive,
        tag.comparableValue,
        tag.categories,
        getTagSection(tag.section),
        tag.description,
        tag.podcastMetadata,
        tag.contributorInformation,
        tag.parents.map(x => EmbeddedEntity[TagEntity](HyperMediaHelpers.tagUri(x))),
        tag.references
      )
  }

  def getTagSection(id: Option[Long]): Option[Section] = {
    id.map( sectionId =>
      SectionRepository.getSection(sectionId)
    ).flatten
  }


  implicit def tagEntityWrites: Writes[TagEntity] = new Writes[TagEntity] {
    def writes(te: TagEntity) = JsObject(Seq(
      "id" -> JsNumber(te.id),
      "path" -> JsString(te.path),
      "pageId" -> JsNumber(te.pageId),
      "type" -> JsString(te.`type`),
      "internalName" -> JsString(te.internalName),
      "externalName" -> JsString(te.externalName),
      "slug" -> JsString(te.slug),
      "hidden" -> JsBoolean(te.hidden),
      "legallySensitive" -> JsBoolean(te.legallySensitive),
      "comparableValue" -> JsString(te.comparableValue),
      "categories" -> JsArray(te.categories.map(JsString(_)).toSeq),
      "section" -> te.section.map(Json.toJson(_)).getOrElse(JsNull),
      "descripton" -> JsString(te.description.getOrElse("")),
      "podcastMetadata" -> te.podcastMetadata.map(Json.toJson(_)).getOrElse(JsNull),
      "contributorInformation" -> te.contributorInformation.map(Json.toJson(_)).getOrElse(JsNull),
      "parents" -> JsArray(te.parents.map(Json.toJson(_)).toSeq)
    ))
  }

}
  // id: Long,
  // path: String,
  // pageId: Long,
  // `type`: String,
  // internalName: String,
  // externalName: String,
  // slug: String,
  // hidden: Boolean = false,
  // legallySensitive: Boolean = false,
  // comparableValue: String,
  // categories: Set[String] = Set(),
  // section: Option[Long],
  // description: Option[String] = None,
  // podcastMetadata: Option[PodcastMetadata] = None,
  // contributorInformation: Option[ContributorInformation] = None,
  // parents: Set[EmbeddedEntity[TagEntity]] = Set(),
  // references: List[Reference] = Nil
