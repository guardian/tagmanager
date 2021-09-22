package model.command

import model.command.CommandError._
import model.jobs.JobHelper
import ai.x.play.json.Jsonx
import ai.x.play.json.Encoders.encoder
import repositories._

import scala.concurrent.{Future, ExecutionContext}

case class BatchTagCommand(contentIds: List[String], toAddToTop: Option[Long], toAddToBottom: List[Long], toAddTrackingTag: List[Long], toRemove: List[Long]) extends Command {
  type T = Unit

  override def process()(implicit username: Option[String] = None, ec: ExecutionContext): Future[Option[Unit]] = Future {
    val toTopList = toAddToTop.toList

    // We'd prefer if people didn't add and remove
    val hasIntersections =
      toTopList.intersect(toRemove).nonEmpty ||
      toTopList.intersect(toAddToBottom).nonEmpty ||
      toTopList.intersect(toAddTrackingTag).nonEmpty ||
      toAddToBottom.intersect(toRemove).nonEmpty

    if (hasIntersections) {
      IntersectingBatchTags
    }

    def toTag(id: Long) = TagRepository.getTag(id).getOrElse(TagNotFound)

    val tops = toAddToTop.map(toTag)
    val bottoms = toAddToBottom.map(toTag)
    val trackingTags = toAddTrackingTag.map(toTag)
    val removals = toRemove.map(toTag)

    JobHelper.buildBatchTagJob(contentIds, tops, bottoms, trackingTags, removals)

    Some(())
  }
}

object BatchTagCommand {
  implicit val batchTagCommandFormat = Jsonx.formatCaseClassUseDefaults[BatchTagCommand]
}
