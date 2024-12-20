package model.command

import com.gu.tagmanagement.{EventType, SectionEvent}
import model.command.CommandError._
import model.{Section, SectionAudit}
import play.api.Logging
import repositories.{PathManager, PathRemoveFailed, SectionAuditRepository, SectionRepository}
import services.KinesisStreams

import scala.concurrent.{Future, ExecutionContext}


case class RemoveEditionFromSectionCommand(sectionId: Long, editionName: String) extends Command with Logging {

  type T = Section

  override def process()(implicit username: Option[String], ec: ExecutionContext): Future[Option[Section]] = Future {
    logger.info(s"removing $editionName from section $sectionId")

    val section = SectionRepository.getSection(sectionId).getOrElse(SectionNotFound)

    val editionInfo = section.editions.getOrElse(editionName.toUpperCase, EditionNotFound)

    val pageId = try { PathManager.removePathForId(editionInfo.pageId) } catch { case p: PathRemoveFailed => PathNotFound}

    val updatedEditions = section.editions.view.filterKeys(_.toUpperCase != editionName.toUpperCase).toMap

    val updatedSection = section.copy(
      editions = updatedEditions,
      discriminator = if (updatedEditions.isEmpty) {
        Some("Navigation")
      } else {
        Some("MultiEdition")
      }
    )

    val result = SectionRepository.updateSection(updatedSection)

    KinesisStreams.sectionUpdateStream.publishUpdate(updatedSection.id.toString, SectionEvent(EventType.Update, updatedSection.id, Some(updatedSection.asThrift)))

    SectionAuditRepository.upsertSectionAudit(SectionAudit.removedEdition(updatedSection, editionName))

    result
  }
}
