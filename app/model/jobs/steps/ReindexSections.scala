package model.jobs.steps

import scala.concurrent.duration._
import play.api.Logger
import services.KinesisStreams
import repositories._
import scala.util.control.NonFatal
import model.jobs.Step

case class ReindexSections() extends Step {
  override def process = {
    val sections = SectionRepository.loadAllSections.toList
    val total = sections.size
    var progress: Int = 0

    try {
      sections.foreach { section =>
        KinesisStreams.reindexSectionsStream.publishUpdate("sectionReindex", section.asThrift)

        progress += 1
        ReindexProgressRepository.updateSectionReindexProgress(progress, total)
      }
      ReindexProgressRepository.completeSectionReindex(progress, total)
    } catch {
      case NonFatal(e) => {
        Logger.error("Section reindex failed", e)
        ReindexProgressRepository.failSectionReindex(progress, total)
        // We need to rethrow the failure to make sure the jobrunner is aware we failed
        throw e
      }
    }
  }

  override def waitDuration: Option[Duration] = {
    None
  }

  override def check: Boolean = {
    true
  }

  override def rollback = {
    throw new UnsupportedOperationException("Rollback is not supported for reindexing sections.")
  }

  override def failureMessage = s"Failed to reindex sections."

  override val `type` = ReindexSections.`type`
}

object ReindexSections {
  val `type` = "reindex-sections"
}
