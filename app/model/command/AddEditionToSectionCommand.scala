package model.command

import com.gu.pandomainauth.model.User
import com.gu.tagmanagement.{EventType, SectionEvent}
import model.command.CommandError._
import model.command.logic.SectionEditionPathCalculator
import model.{Section, SectionAudit, EditionalisedPage}
import play.api.Logger
import repositories.{PathRegistrationFailed, PathManager, SectionAuditRepository, SectionRepository}
import services.KinesisStreams


case class AddEditionToSectionCommand(sectionId: Long, editionName: String) extends Command {

  type T = Section

  override def process()(implicit user: Option[User] = None): Option[Section] = {
    Logger.info(s"add ${editionName} to section ${sectionId}")

    val section = SectionRepository.getSection(sectionId).getOrElse(SectionNotFound)

    val calculatedPath = SectionEditionPathCalculator.calculatePath(section, editionName)

    val pageId = try { PathManager.registerPathAndGetPageId(calculatedPath) } catch { case p: PathRegistrationFailed => PathInUse}

    val updatedSection = section.copy(
      editions = section.editions ++ Map(editionName -> EditionalisedPage(calculatedPath, pageId))
    )

    val result = SectionRepository.updateSection(updatedSection)

    KinesisStreams.sectionUpdateStream.publishUpdate(updatedSection.id.toString, SectionEvent(EventType.Update, updatedSection.id, Some(updatedSection.asThrift)))

    SectionAuditRepository.upsertSectionAudit(SectionAudit.addedEdition(updatedSection, editionName))

    result
  }
}
