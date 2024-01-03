package model.command

import com.gu.tagmanagement.{EventType, SectionEvent}
import model.{Section, SectionAudit}
import play.api.Logging
import repositories.{SectionAuditRepository, SectionRepository}
import services.KinesisStreams

import scala.concurrent.{Future, ExecutionContext}


case class UpdateSectionCommand(section: Section) extends Command with Logging {

  type T = Section

  override def process()(implicit username: Option[String], ec: ExecutionContext): Future[Option[Section]] = Future{
    logger.info(s"updating section ${section.id}")

    val result = SectionRepository.updateSection(section)

    KinesisStreams.sectionUpdateStream.publishUpdate(section.id.toString, SectionEvent(EventType.Update, section.id, Some(section.asThrift)))

    SectionAuditRepository.upsertSectionAudit(SectionAudit.updated(section))

    result
  }
}
