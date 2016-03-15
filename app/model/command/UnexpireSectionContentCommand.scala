package model.command

import java.nio.ByteBuffer

import com.gu.tagmanagement._
import model.{Tag, TagAudit, Section}
import org.joda.time.{DateTime, DateTimeZone}
import play.api.Logger
import repositories.{SectionRepository, ContentAPI, TagAuditRepository, TagRepository}
import com.amazonaws.services.kinesis.model.{PutRecordRequest, PutRecordResult}
import services.{KinesisStreams, Config}


case class UnexpireSectionContentCommand(sectionId: Long) extends Command {

  type T = Section

  override def process()(implicit username: Option[String] = None): Option[Section] = {
    Logger.info(s"Unexpiring Content for Section: ${sectionId}")

    SectionRepository.getSection(sectionId).map(section => {

      val contentIds = ContentAPI.getContentIdsForSection(section.path)

      contentIds.foreach(contentId => {
        Logger.info(s"Triggering unexpiry for content $contentId")

        KinesisStreams.commercialExpiryStream.publishUpdate(contentId, false.toString)
      })

      section
    })
  }
}
