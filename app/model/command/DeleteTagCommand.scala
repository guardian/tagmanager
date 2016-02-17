package model.command

import com.gu.tagmanagement.{TagWithSection, OperationType, TaggingOperation}
import model.command.CommandError._
import model.jobs.{TagRemovedCheck, RemoveTagStep, AllUsagesOfTagRemovedCheck, Job}
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Format}
import repositories._
import services.{SQS, KinesisStreams}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


case class DeleteTagCommand(removingTagId: Long) extends Command {
  override type T = Long

  override def process()(implicit username: Option[String] = None): Option[T] = {
    val removingTag = TagLookupCache.getTag(removingTagId) getOrElse(TagNotFound)
    val removingTagSection = removingTag.section.flatMap( SectionLookupCache.getSection(_) )

    val jobId = Sequences.jobId.getNextId

    Future {

      val contentIds = ContentAPI.getContentIdsForTag(removingTag.path)

      val deleteTagJob = Job(
        id = jobId,
        `type` = "Delete tag",
        started = new DateTime,
        startedBy = username,
        tagIds = List(removingTagId),
        command = this,
        steps = List(
          AllUsagesOfTagRemovedCheck(removingTag.path, contentIds.length),
          RemoveTagStep(removingTagId, username),
          TagRemovedCheck(removingTag.path)
        )
      )

      Logger.info(s"raising job to check deleting ${removingTag.path}  completes")
      JobRepository.upsertJob(deleteTagJob)
      SQS.jobQueue.postMessage(deleteTagJob.id.toString, delaySeconds = 15)


      contentIds foreach { contentPath =>
        val taggingOperation = TaggingOperation(
          operation = OperationType.Remove,
          contentPath = contentPath,
          tag = Some(TagWithSection(removingTag.asThrift, removingTagSection.map(_.asThrift)))
        )
        Logger.info(s"raising delete tag ${removingTag.path} for content $contentPath")
        KinesisStreams.taggingOperationsStream.publishUpdate(contentPath.take(200), taggingOperation)
      }
    }

    Some(jobId)

  }
}

object DeleteTagCommand {
  implicit val deleteTagCommandFormat: Format[DeleteTagCommand] = (
    (JsPath \ "removingTagId")
  ).format[Long].inmap(id => DeleteTagCommand(id), (deleteTagCommand: DeleteTagCommand) => deleteTagCommand.removingTagId)
}
