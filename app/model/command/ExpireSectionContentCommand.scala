package model.command

import model.Section
import play.api.Logger
import repositories._
import services.KinesisStreams


case class ExpireSectionContentCommand(sectionId: Long) extends Command {

  type T = Section

  override def process()(implicit username: Option[String] = None): Option[Section] = {
    Logger.info(s"Expiring Content for Section: ${sectionId}")

    SectionRepository.getSection(sectionId).map(section => {

      val contentIds = ContentAPI.getDraftContentIdsForSection(section.path)

      contentIds.foreach(contentId => {
        Logger.info(s"Triggering unexpiry for content $contentId")
        KinesisStreams.commercialExpiryStream.publishUpdate(contentId, true.toString)
      })

      section
    })
  }
}
