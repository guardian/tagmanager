package model.command

import model.Section
import play.api.Logger
import repositories._
import services.{Contexts, KinesisStreams}

import scala.concurrent.Future


case class UnexpireSectionContentCommand(sectionId: Long) extends Command {

  type T = Section

  override def process()(implicit username: Option[String] = None): Future[Option[Section]] = Future{
    Logger.info(s"Unexpiring Content for Section: $sectionId")

    SectionRepository.getSection(sectionId).map(section => {

      val contentIds = ContentAPI.getDraftContentIdsForSection(section.path)

      contentIds.foreach(contentId => {
        Logger.info(s"Triggering unexpiry for content $contentId")

        KinesisStreams.commercialExpiryStream.publishUpdate(contentId, false.toString)
      })

      section
    })
  }(Contexts.tagOperationContext)
}
