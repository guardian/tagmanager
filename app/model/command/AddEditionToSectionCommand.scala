package model.command

import com.gu.tagmanagement.{EventType, SectionEvent}
import model.command.CommandError._
import model.command.logic.SectionEditionPathCalculator
import model.{EditionalisedPage, Section, SectionAudit}
import play.api.Logger
import repositories.{PathManager, PathRegistrationFailed, SectionAuditRepository, SectionRepository}
import services.{Contexts, KinesisStreams}

import scala.concurrent.Future


case class AddEditionToSectionCommand(sectionId: Long, editionName: String) extends Command {

  type T = Section

  override def process()(implicit username: Option[String] = None): Future[Option[Section]] = Future{
    Logger.info(s"add $editionName to section $sectionId")

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
  }(Contexts.tagOperationContext)
}
