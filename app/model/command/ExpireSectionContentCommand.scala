package model.command

import model.Section
import play.api.Logging
import repositories._
import services.KinesisStreams

import scala.concurrent.{Future, ExecutionContext}


case class ExpireSectionContentCommand(sectionId: Long) extends Command with Logging {

  type T = Section

  override def process()(implicit username: Option[String] = None, ec: ExecutionContext): Future[Option[Section]] = Future{
    logger.info(s"Expiring Content for Section: $sectionId")

    SectionRepository.getSection(sectionId).map(section => {

      val contentIds = ContentAPI.getDraftContentIdsForSection(section.path)

      contentIds.foreach(contentId => {
        logger.info(s"Triggering unexpiry for content $contentId")
        KinesisStreams.commercialExpiryStream.publishUpdate(contentId, true.toString)
      })

      section
    })
  }
}
