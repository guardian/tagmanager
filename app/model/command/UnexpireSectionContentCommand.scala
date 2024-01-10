package model.command

import model.Section
import play.api.Logging
import repositories._
import services.KinesisStreams

import scala.concurrent.{Future, ExecutionContext}


case class UnexpireSectionContentCommand(sectionId: Long) extends Command with Logging {

  type T = Section

  override def process()(implicit username: Option[String], ec: ExecutionContext): Future[Option[Section]] = Future{
    logger.info(s"Unexpiring Content for Section: $sectionId")

    SectionRepository.getSection(sectionId).map(section => {

      val contentIds = ContentAPI.getDraftContentIdsForSection(section.path)

      contentIds.foreach(contentId => {
        logger.info(s"Triggering unexpiry for content $contentId")

        KinesisStreams.commercialExpiryStream.publishUpdate(contentId, false.toString)
      })

      section
    })
  }
}
