package model.jobs.steps

import com.gu.tagmanagement.{OperationType, TagWithSection, TaggingOperation}
import model.{BatchTagOperation, Section, Tag, TagAudit}
import model.jobs.StepStatus

import scala.concurrent.duration._
import model.jobs.Step
import play.api.Logger
import services.KinesisStreams
import repositories.{ContentAPI, TagAuditRepository}

import scala.language.postfixOps

// The 'op' argument of this case class should really be the BatchTagOperation type but serializing enums to JSON isn't very easy with the
// current version of play and enumeratim
case class ModifyContentTags(tag: Tag, section: Option[Section], contentIds: List[String], op: String, `type`: String = ModifyContentTags.`type`, var stepStatus: String = StepStatus.ready, var stepMessage: String = "Waiting", var attempts: Int = 0) extends Step {
  private val MAX_PARTITION_KEY_LENGTH = 128

  override def process = {
    contentIds foreach { contentPath =>
      val taggingOperation = TaggingOperation(
        operation = OperationType.valueOf(op.replace("-", "")).get,
        contentPath = contentPath,
        tag = Some(TagWithSection(tag.asThrift, section.map(_.asThrift)))
      )
      KinesisStreams.taggingOperationsStream.publishUpdate(contentPath.take(MAX_PARTITION_KEY_LENGTH), taggingOperation)
      Logger.info(s"raising $op for ${tag.id} on $contentPath operation")
    }
    TagAuditRepository.upsertTagAudit(TagAudit.batchTag(tag, op, contentIds.length))
  }

  override def waitDuration: Option[Duration] = {
    Some(5 seconds)
  }

  override def check: Boolean = {
    val count = ContentAPI.countOccurencesOfTagInContents(contentIds, tag.path)

    // As stated above, once we get the BatchTagOperation enum type serializable we can match against a type here instead of a string
    val expected = op match {
      case "remove" => 0
      case _ => contentIds.length
    }

    Logger.info(s"Checking batch tag operations. Expected=$expected Actual=$count")

    count == expected
  }

  override def rollback = {
    throw new UnsupportedOperationException("Rollback is not supported for modifying tags on content.")
  }

  // TODO replace with nice enum type (Do I really expect this will ever happen? Well we all have dreams!)
  private def humanReadableOp = op match {
    case "remove" => "removed"
    case "add-to-bottom" => "added to bottom"
    case "add-to-top" => "added to top"
  }

  override val checkingMessage = s"Checking if '${tag.path}' has been $humanReadableOp to content in CAPI."
  override val failureMessage = s"Failed to issue the tagging operation for '${tag.path}' to content."
  override val checkFailMessage = s"Tag '${tag.path}' was not $humanReadableOp on all content in CAPI."
}

object ModifyContentTags {
  val `type` = "modify-content-tags"
}
