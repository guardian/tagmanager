package model.command

import com.gu.pandomainauth.model.User
import com.gu.tagmanagement.{EventType, SectionEvent}
import model.command.CommandError._
import model.command.logic.SectionEditionPathCalculator
import model.{EditionalisedPage, Section, SectionAudit}
import play.api.Logger
import repositories.{PathManager, PathRegistrationFailed, PathRemoveFailed, SectionAuditRepository, SectionRepository}
import services.KinesisStreams


case class RemoveEditionFromSectionCommand(sectionId: Long, editionName: String) extends Command {

  type T = Section

  override def process()(implicit user: Option[User] = None): Option[Section] = {
    Logger.info(s"removing ${editionName} from section ${sectionId}")

    val section = SectionRepository.getSection(sectionId).getOrElse(SectionNotFound)

    val editionInfo = section.editions.get(editionName.toUpperCase).getOrElse(EditionNotFound)

    val pageId = try { PathManager.removePathForId(editionInfo.pageId) } catch { case p: PathRemoveFailed => PathNotFound}

    val updatedSection = section.copy(
      editions = section.editions.filterKeys(_.toUpperCase != editionName.toUpperCase)
    )

    val result = SectionRepository.updateSection(updatedSection)

    KinesisStreams.sectionUpdateStream.publishUpdate(updatedSection.id.toString, SectionEvent(EventType.Update, updatedSection.id, Some(updatedSection.asThrift)))

    SectionAuditRepository.upsertSectionAudit(SectionAudit.removedEdition(updatedSection, editionName))

    result
  }
}
