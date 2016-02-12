package model
import play.api.libs.json._
import repositories.SectionRepository

/* Flex expects the old api so this essentially mimics the old functionality
 * by adding a SectionEntity and TagEntity. It does mean that some of the new
 * data about tags and sections is missing but this can be rectified by altering
 * flex.
 */

case class TagEntity(
  id: Long,
  `type`: String,
  internalName: String,
  externalName: String,
  slug: String,
  section: SectionEntity,
  parents: Set[EmbeddedEntity[TagEntity]] = Set(),
  references: List[Reference] = Nil
)

object TagEntity {
  def apply(tag: Tag): TagEntity = {
    /* The Section is implicitly populated when this is called */
      TagEntity(
        tag.id,
        tag.`type`,
        tag.internalName,
        tag.externalName,
        tag.slug,
        getTagSection(tag.section),
        tag.parents.map(x => EmbeddedEntity[TagEntity](HyperMediaHelpers.tagUri(x))),
        tag.externalReferences
      )
  }

  def getTagSection(id: Option[Long]): SectionEntity = {
    val section = id.map(sectionId =>
      SectionRepository.getSection(sectionId).map(section =>
        SectionEntity(section))
      ).flatten

    // else use the global section
    val globalSection = SectionEntity(281, "Global", "global", "global")

    section getOrElse globalSection
  }


  implicit def tagEntityWrites: Writes[TagEntity] = new Writes[TagEntity] {
    def writes(te: TagEntity) = JsObject(Seq(
      "id" -> JsNumber(te.id),
      "type" -> JsString(te.`type`),
      "internalName" -> JsString(te.internalName),
      "externalName" -> JsString(te.externalName),
      "slug" -> JsString(te.slug),
      "section" -> Json.toJson(te.section),
      "parents" -> JsArray(te.parents.map(Json.toJson(_)).toSeq),
      "externalReferences" -> JsArray(te.references.map(Json.toJson(_)))

    ))
  }

}

case class SectionEntity(
  id: Long,
  name: String,
  pathPrefix: String,
  slug: String
)

object SectionEntity {
  implicit def sectionEntityWrites: Writes[SectionEntity] = new Writes[SectionEntity] {
    def writes(se: SectionEntity) = JsObject(Seq(
      "id" -> JsNumber(se.id),
      "name" -> JsString(se.name),
      "pathPrefix" -> JsString(se.pathPrefix),
      "slug" -> JsString(se.slug)
    ))
  }

  def apply(section: Section): SectionEntity = {
    SectionEntity(
      section.id,
      section.name,
      section.path,
      section.wordsForUrl
    )
  }
}
