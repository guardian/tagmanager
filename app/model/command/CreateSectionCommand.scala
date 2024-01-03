package model.command

import com.gu.tagmanagement.{EventType, SectionEvent}
import model._
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}
import repositories._
import CommandError._
import services.KinesisStreams

import scala.concurrent.{Future, ExecutionContext}

case class CreateSectionCommand(
                                 name: String,
                                 wordsForUrl: String,
                                 editions: Map[String, EditionalisedPage] = Map(),
                                 discriminator: Option[String] = None,
                                 isMicrosite: Boolean

        ) extends Command {

          type T = Section

          def process()(implicit username: Option[String], ec: ExecutionContext): Future[Option[Section]] = {

            val calculatedPath = wordsForUrl

            val pageIdFuture: Future[Long] = Future{try { PathManager.registerPathAndGetPageId(calculatedPath) } catch { case p: PathRegistrationFailed => PathInUse}}

            val sectionIdFuture = Future{Sequences.sectionId.getNextId}

            def sectionTag(sectionId: Long): Future[Option[Tag]] = CreateTagCommand(
              `type` = "Topic",
              internalName = name,
              externalName = name,
              slug = wordsForUrl,
              comparableValue = name,
              section = Some(sectionId),
              isMicrosite = isMicrosite,
              preCalculatedPath = Some(s"$wordsForUrl/$wordsForUrl")
            ).process()

            for {
              pageId <- pageIdFuture
              sectionId <- sectionIdFuture
              sectionTag <- sectionTag(sectionId)
              sectionTagId = sectionTag.map(_.id).getOrElse(CouldNotCreateSectionTag)
            } yield {
              val section = Section(
                id = sectionId,
                sectionTagId = sectionTagId,
                name = name,
                path = calculatedPath,
                wordsForUrl = wordsForUrl,
                pageId = pageId,
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
        }

        object CreateSectionCommand {

          implicit val createSectionCommandFormat: Format[CreateSectionCommand] = (
          (JsPath \ "name").format[String] and
          (JsPath \ "wordsForUrl").format[String] and
          (JsPath \ "editions").formatNullable[Map[String, EditionalisedPage]].inmap[Map[String, EditionalisedPage]](_.getOrElse(Map()), Some(_)) and
          (JsPath \ "discriminator").formatNullable[String] and
          (JsPath \ "isMicrosite").format[Boolean]

          )(CreateSectionCommand.apply, unlift(CreateSectionCommand.unapply))
        }
