package model.command

import com.gu.tagmanagement.{EventType, SectionEvent}
import model.command.CommandError._
import model.command.logic.SectionEditionPathCalculator
import model.{EditionalisedPage, Section, SectionAudit}
import play.api.Logging
import repositories.{PathManager, PathRegistrationFailed, SectionAuditRepository, SectionRepository}
import services.KinesisStreams

import scala.concurrent.{Future, ExecutionContext}


case class AddEditionToSectionCommand(sectionId: Long, editionName: String) extends Command with Logging {

  type T = Section

  override def process()(implicit username: Option[String] = None, ec: ExecutionContext): Future[Option[Section]] = Future{
    logger.info(s"add $editionName to section $sectionId")

    val section = SectionRepository.getSection(sectionId).getOrElse(SectionNotFound)

    val calculatedPath = SectionEditionPathCalculator.calculatePath(section, editionName)

    val pageId = try { PathManager.registerPathAndGetPageId(calculatedPath) } catch { case p: PathRegistrationFailed => PathInUse}

    val updatedEditions = section.editions ++ Map(editionName -> EditionalisedPage(calculatedPath, pageId))

    val updatedSection = section.copy(
      editions = updatedEditions,
      discriminator = updatedEditions.isEmpty match {
        case false => Some("MultiEdition")
        case true => Some("Navigation")
      }
    )

    val result = SectionRepository.updateSection(updatedSection)

    KinesisStreams.sectionUpdateStream.publishUpdate(updatedSection.id.toString, SectionEvent(EventType.Update, updatedSection.id, Some(updatedSection.asThrift)))

    SectionAuditRepository.upsertSectionAudit(SectionAudit.addedEdition(updatedSection, editionName))

    result
  }
}
