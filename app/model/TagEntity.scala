package model
import play.api.libs.json._
import repositories.SectionRepository

//used for responses in the hypermedia api

case class TagEntity(
  id: Long,
  `type`: String,
  internalName: String,
  externalName: String,
  slug: String,
  section: Option[SectionEntity],
  parents: Set[EmbeddedEntity[TagEntity]] = Set(),
  references: List[Reference] = Nil
)

object TagEntity {
  def apply(tag: Tag): TagEntity = {
      TagEntity(
        tag.id,
        tag.`type`,
        tag.internalName,
        tag.externalName,
        tag.slug,
        getTagSection(tag.section),
        tag.parents.map(x => EmbeddedEntity[TagEntity](HyperMediaHelpers.tagUri(x))),
        tag.references
      )
  }

  def getTagSection(id: Option[Long]): Option[SectionEntity] = {
    id.map(sectionId =>
      SectionRepository.getSection(sectionId).map(section =>
        SectionEntity(section))
      ).flatten
  }


  implicit def tagEntityWrites: Writes[TagEntity] = new Writes[TagEntity] {
    def writes(te: TagEntity) = JsObject(Seq(
      "id" -> JsNumber(te.id),
      "type" -> JsString(te.`type`),
      "internalName" -> JsString(te.internalName),
      "externalName" -> JsString(te.externalName),
      "slug" -> JsString(te.slug),
      "section" -> te.section.map(Json.toJson(_)).getOrElse(JsNull),
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
