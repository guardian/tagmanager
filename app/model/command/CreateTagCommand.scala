package model.command

import com.gu.tagmanagement.{EventType, TagEvent}
import model.command.logic.TagPathCalculator
import model.{Tag, Reference}
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Format}
import repositories._
import CommandError._
import services.KinesisStreams


case class CreateTagCommand(
                      `type`: String,
                      internalName: String,
                      externalName: String,
                      slug: String,
                      hidden: Boolean = false,
                      legallySensitive: Boolean = false,
                      comparableValue: String,
                      categories: Set[String] = Set(),
                      section: Option[Long],
                      description: Option[String] = None,
                      parents: Set[Long] = Set(),
                      references: List[Reference] = Nil
                      ) extends Command[Tag] {

  def process = {

    val calculatedPath = TagPathCalculator.calculatePath(`type`, slug, section)

    val pageId = try { PathManager.registerPathAndGetPageId(calculatedPath) } catch { case p: PathRegistrationFailed => PathInUse}

    val tag = Tag(
      id = Sequences.tagId.getNextId,
      path = calculatedPath,
      pageId = pageId,
      `type`= `type`,
      internalName = internalName,
      externalName = externalName,
      slug = slug,
      hidden = hidden,
      legallySensitive = legallySensitive,
      comparableValue = comparableValue,
      section = section,
      categories = categories,
      description = description,
      parents = parents,
      references = references
    )

    val result = TagRepository.createTag(tag)

    KinesisStreams.tagUpdateStream.publishUpdate(tag.id.toString, TagEvent(EventType.Create, tag.id, Some(tag.asThrift)))

    result
  }
}

object CreateTagCommand {

  implicit val tagFormat: Format[CreateTagCommand] = (
    (JsPath \ "type").format[String] and
      (JsPath \ "internalName").format[String] and
      (JsPath \ "externalName").format[String] and
      (JsPath \ "slug").format[String] and
      (JsPath \ "hidden").format[Boolean] and
      (JsPath \ "legallySensitive").format[Boolean] and
      (JsPath \ "comparableValue").format[String] and
      (JsPath \ "categories").formatNullable[Set[String]].inmap[Set[String]](_.getOrElse(Set()), Some(_)) and
      (JsPath \ "section").formatNullable[Long] and
      (JsPath \ "description").formatNullable[String] and
      (JsPath \ "parents").formatNullable[Set[Long]].inmap[Set[Long]](_.getOrElse(Set()), Some(_)) and
      (JsPath \ "externalReferences").formatNullable[List[Reference]].inmap[List[Reference]](_.getOrElse(Nil), Some(_))
    )(CreateTagCommand.apply, unlift(CreateTagCommand.unapply))
}
