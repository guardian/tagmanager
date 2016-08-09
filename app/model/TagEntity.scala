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
  references: List[ReferenceEntity] = Nil,
  path: String,
  subType: Option[String]
)

object TagEntity {
  def apply(tag: Tag): TagEntity = {
    /* The Section is implicitly populated when this is called */

    val convertedType = tag.`type`.toLowerCase match {
      case "topic" => "Keyword"
      case "contenttype" => "Content Type"
      case "newspaperbook" => "Newspaper Book"
      case "newspaperbooksection" => "Newspaper Book Section"
      case _ => tag.`type`
    }

    val parents = tag.publication match {
      case Some(publications) => tag.parents ++ Set(publications)
      case None => tag.parents
    }

    val subtype = tag.trackingInformation.map(_.trackingType) orElse( tag.paidContentInformation.map(_.paidContentType))

      TagEntity(
        tag.id,
        convertedType,
        tag.internalName,
        tag.externalName,
        tag.slug,
        getTagSection(tag.section),
        parents.map(x => EmbeddedEntity[TagEntity](HyperMediaHelpers.tagUri(x))),
        tag.externalReferences.map(ReferenceEntity(_)),
        tag.path,
        subtype
      )
  }

  def getTagSection(id: Option[Long]): SectionEntity = {
    val section = id.map(sectionId =>
      SectionRepository.getSection(sectionId).map(section =>
        SectionEntity(section))
      ).flatten

    // else use the global section
    val globalSection = SectionEntity(281, "Global", "global", "global", 14821)

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
      "externalReferences" -> JsArray(te.references.map(Json.toJson(_))),
      "path" -> JsString(te.path)
    ) ++ te.subType.map("subType" -> JsString(_)) )
  }

}

case class SectionEntity(
  id: Long,
  name: String,
  pathPrefix: String,
  slug: String,
  sectionTagId: Long
)

object SectionEntity {
  implicit def sectionEntityWrites: Writes[SectionEntity] = new Writes[SectionEntity] {
    def writes(se: SectionEntity) = JsObject(Seq(
      "id" -> JsNumber(se.id),
      "name" -> JsString(se.name),
      "pathPrefix" -> JsString(se.pathPrefix),
      "sectionTagId" -> JsNumber(se.sectionTagId),
      "slug" -> JsString(se.slug)
    ))
  }

  def apply(section: Section): SectionEntity = {
    SectionEntity(
      section.id,
      section.name,
      section.path,
      section.wordsForUrl,
      section.sectionTagId
    )
  }
}

case class ReferenceEntity(
  `type`: String,
  token: String
)

object ReferenceEntity {
  implicit def referenceEntityWrites: Writes[ReferenceEntity] = new Writes[ReferenceEntity] {
    def writes(re: ReferenceEntity) = JsObject(Seq(
      "type" -> JsString(re.`type`),
      "token" -> JsString(re.token)
    ))
  }

  def apply(reference: Reference): ReferenceEntity = {
    ReferenceEntity(
      reference.`type`,
      reference.value
    )
  }
}
