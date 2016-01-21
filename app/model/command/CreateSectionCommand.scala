package model.command

import com.gu.tagmanagement.{EventType, SectionEvent}
import model.command.logic.TagPathCalculator
import model._
import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Format}
import repositories._
import CommandError._
import services.KinesisStreams


case class CreateSectionCommand(
                                 sectionTagId: Long,
                                 name: String,
                                 wordsForUrl: String,
                                 editions: Map[String, EditionalisedPage] = Map(),
                                 discriminator: Option[String] = None,
                                 isMicrosite: Boolean

        ) extends Command {

          type T = Tag

          def process()(implicit username: Option[String] = None): Option[Section] = {

              val calculatedPath = wordsForUrl

              val pageId = try { PathManager.registerPathAndGetPageId(calculatedPath) } catch { case p: PathRegistrationFailed => PathInUse}

              val sectionId = Sequences.sectionId.getNextId

              val sectionTagId = CreateTagCommand(
                `type` = "Topic",
                internalName = name,
                externalName = name,
                slug = wordsForUrl,
                comparableValue = name,
                section = Some(sectionId),
                isMicrosite = isMicrosite
              ).process().map(_.id) getOrElse CouldNotCreateSectionTag

              val section = Section(
                id = sectionId,
                path = calculatedPath,
                pageId = pageId,
                sectionTagId = sectionTagId,
                name = name,
                wordsForUrl = wordsForUrl,
                editions = editions,
                discriminator = discriminator,
                isMicrosite = isMicrosite
              )

              val result = SectionRepository.updateSection(section)

              KinesisStreams.sectionUpdateStream.publishUpdate(section.id.toString, SectionEvent(EventType.Update, section.id, Some(section.asThrift)))

              SectionAuditRepository.upsertSectionAudit(SectionAudit.created(section))

              result
          }
        }

        object CreateSectionCommand {

          implicit val createTagCommandFormat: Format[CreateSectionCommand] = (
          (JsPath \ "sectionTagId").format[Long] and
          (JsPath \ "name").format[String] and
          (JsPath \ "wordsForUrl").format[String] and
          (JsPath \ "editions").formatNullable[Map[String, EditionalisedPage]].inmap[Map[String, EditionalisedPage]](_.getOrElse(Map()), Some(_)) and
          (JsPath \ "discriminator").formatNullable[String] and
          (JsPath \ "isMicrosite").format[Boolean]

          )(CreateSectionCommand.apply, unlift(CreateSectionCommand.unapply))
        }

