package model.command

import com.gu.pandomainauth.model.User
import com.gu.tagmanagement.{OperationType, TaggingOperation}
import model.jobs.{BatchTagAddCompleteCheck, BatchTagRemoveCompleteCheck, Job}
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Format}
import repositories.{JobRepository, Sequences, TagRepository}
import CommandError._
import services.{SQS, KinesisStreams}

case class BatchTagCommand(contentIds: List[String], tagId: Long, operation: String) extends Command {
  type T = Long

  override def process()(implicit user: Option[User] = None): Option[Long] = {
    val tag = TagRepository.getTag(tagId) getOrElse(TagNotFound)

    contentIds foreach { contentPath =>
      val taggingOperation = TaggingOperation(
        operation = OperationType.valueOf(operation).get,
        contentPath = contentPath,
        tag = Some(tag.asThrift)
      )
      Logger.info(s"raising $operation for $tagId on $contentPath operation")
      KinesisStreams.taggingOperationsStream.publishUpdate(contentPath.take(200), taggingOperation)
    }

    val batchTagJob = Job(
      id = Sequences.jobId.getNextId,
      `type` = "Batch tag",
      started = new DateTime,
      startedBy = user.map(_.email),
      command = this,
      steps = operation match {
        case "remove" => List(BatchTagRemoveCompleteCheck(contentIds, tag.path))
        case _ => List(BatchTagAddCompleteCheck(contentIds, tag.path))
      }
    )

    Logger.info(s"raising job to check $operation for $tagId completes")
    JobRepository.upsertJob(batchTagJob)
    SQS.jobQueue.postMessage(batchTagJob.id.toString, delaySeconds = 15)

    Some(batchTagJob.id)
  }
}

object BatchTagCommand {
  implicit val batchTagCommandFormat: Format[BatchTagCommand] = (
      (JsPath \ "contentIds").formatNullable[List[String]].inmap[List[String]](_.getOrElse(Nil), Some(_)) and
      (JsPath \ "tagId").format[Long] and
      (JsPath \ "operation").format[String]
    )(BatchTagCommand.apply, unlift(BatchTagCommand.unapply))
}
