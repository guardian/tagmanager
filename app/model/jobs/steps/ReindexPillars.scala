package model.jobs.steps

import com.gu.tagmanagement.{PillarEvent, PillarEventType}
import model.Pillar

import scala.concurrent.duration._
import play.api.Logging
import services.KinesisStreams
import repositories._

import scala.util.control.NonFatal
import model.jobs.{Step, StepStatus}

case class ReindexPillars(
  `type`: String = ReindexPillars.`type`,
  var stepStatus: String = StepStatus.ready,
  var stepMessage: String = "Waiting",
  var attempts: Int = 0
) extends Step
  with Logging {

  override def process = {
    val pillars = PillarRepository.loadAllPillars.toList
    val total = pillars.size
    var progress: Int = 0

    logger.info("Starting pillar reindex")
    try {
      pillars.foreach { pillar =>
        val pillarEvent = PillarEvent(
          PillarEventType.Update,
          pillar.id,
          Some(Pillar.asThrift(pillar))
        )
        KinesisStreams.reindexPillarsStream.publishUpdate("pillarReindex", pillarEvent)

        progress += 1
        ReindexProgressRepository.updatePillarReindexProgress(progress, total)
      }
      ReindexProgressRepository.completePillarReindex(progress, total)
    } catch {
      case NonFatal(e) => {
        logger.error("Pillar reindex failed", e)
        ReindexProgressRepository.failPillarReindex(progress, total)
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
    throw new UnsupportedOperationException("Rollback is not supported for reindexing pillars.")
  }

  override val checkingMessage = s"Checking pillar reindex was successful" // Should not happen
  override val failureMessage = s"Failed to reindex pillars."
  override val checkFailMessage = s"Failed to confirm pillar reindex was successful." // Should not happen
}

object ReindexPillars {
  val `type` = "reindex-pillars"
}
