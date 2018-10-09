package model.command

import model.command.CommandError._
import model.jobs.JobHelper
import org.cvogt.play.json.Jsonx
import repositories._
import services.Contexts

import scala.concurrent.Future

case class BatchTagCommand(contentIds: List[String], toAddToTop: Option[Long], toAddToBottom: List[Long], toRemove: List[Long]) extends Command {
  type T = Unit

  override def process()(implicit username: Option[String] = None): Future[Option[Unit]] = Future {
    val toTopList = toAddToTop.toList

    // We'd prefer if people didn't add and remove
    val hasIntersections =
      toTopList.intersect(toRemove).nonEmpty ||
      toTopList.intersect(toAddToBottom).nonEmpty ||
      toAddToBottom.intersect(toRemove).nonEmpty

    if (hasIntersections) {
      IntersectingBatchTags
    }

    def toTag(id: Long) = TagRepository.getTag(id).getOrElse(TagNotFound)

    val tops = toAddToTop.map(toTag)
    val bottoms = toAddToBottom.map(toTag)
    val removals = toRemove.map(toTag)

    JobHelper.buildBatchTagJob(contentIds, tops, bottoms, removals)

    Some(())
  }(Contexts.tagOperationContext)
}

object BatchTagCommand {
  implicit val batchTagCommandFormat = Jsonx.formatCaseClassUseDefaults[BatchTagCommand]
}
