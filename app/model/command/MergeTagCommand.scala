package model.command

import com.gu.tagmanagement.{TagWithSection, OperationType, TaggingOperation}
import model.command.CommandError._
import model.jobs.{TagRemovedCheck, RemoveTagStep, AllUsagesOfTagRemovedCheck, Job, MergeAuditStep}
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Format}
import repositories._
import services.{SQS, KinesisStreams}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


case class MergeTagCommand(removingTagId: Long, replacementTagId: Long) extends Command {
  override type T = Long

  override def process()(implicit username: Option[String] = None): Option[T] = {
    val removingTag = TagLookupCache.getTag(removingTagId) getOrElse(TagNotFound)
    val removingTagSection = removingTag.section.flatMap( SectionLookupCache.getSection(_) )

    val replacementTag = TagLookupCache.getTag(replacementTagId) getOrElse(TagNotFound)
    val replacementTagSection = replacementTag.section.flatMap( SectionLookupCache.getSection(_) )

    val jobId = Sequences.jobId.getNextId
    Future {

      val contentIds = ContentAPI.getContentIdsForTag(removingTag.path)

      val mergeTagJob = Job(
        id = jobId,
        `type` = "Merge tag",
        started = new DateTime,
        startedBy = username,
        tagIds = List(removingTagId, replacementTagId),
        command = this,
        steps = List(
          AllUsagesOfTagRemovedCheck(removingTag.path, contentIds.length),
          RemoveTagStep(removingTagId, username),
          TagRemovedCheck(removingTag.path),
          MergeAuditStep(removingTag.id, replacementTag.id, username)
        )
      )
      Logger.info(s"raising job to check merging ${removingTag.path} into ${replacementTag.path} completes")
      JobRepository.upsertJob(mergeTagJob)
      SQS.jobQueue.postMessage(mergeTagJob.id.toString, delaySeconds = 15)


      contentIds foreach { contentPath =>
        val taggingOperation = TaggingOperation(
          operation = OperationType.Merge,
          contentPath = contentPath,
          tag = Some(TagWithSection(removingTag.asThrift, removingTagSection.map(_.asThrift))),
          destinationTag = Some(TagWithSection(replacementTag.asThrift, replacementTagSection.map(_.asThrift)))
        )
        Logger.info(s"raising merge tag ${removingTag.path} -> ${replacementTag.path} for content $contentPath")
        KinesisStreams.taggingOperationsStream.publishUpdate(contentPath.take(200), taggingOperation)
      }
    }

    Some(jobId)

  }
}

object MergeTagCommand {
  implicit val mergeTagCommandFormat: Format[MergeTagCommand] = (
      (JsPath \ "removingTagId").format[Long] and
      (JsPath \ "replacementTagId").format[Long]
    )(MergeTagCommand.apply, unlift(MergeTagCommand.unapply))
}
