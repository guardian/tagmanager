package model.command

import com.gu.tagmanagement.{EventType, SectionEvent}
import model.{Section, SectionAudit}
import play.api.Logger
import repositories.{SectionAuditRepository, SectionRepository}
import services.{Contexts, KinesisStreams}

import scala.concurrent.Future


case class UpdateSectionCommand(section: Section) extends Command {

  type T = Section

  override def process()(implicit username: Option[String] = None): Future[Option[Section]] = Future{
    Logger.info(s"updating section ${section.id}")

    val result = SectionRepository.updateSection(section)

    KinesisStreams.sectionUpdateStream.publishUpdate(section.id.toString, SectionEvent(EventType.Update, section.id, Some(section.asThrift)))

    SectionAuditRepository.upsertSectionAudit(SectionAudit.updated(section))

    result
  }(Contexts.tagOperationContext)
}
