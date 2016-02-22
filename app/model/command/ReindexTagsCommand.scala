package model.command

import model.AppAudit;
import model.jobs.{Job, ReindexTags}
import services.SQS
import org.cvogt.play.json.Jsonx
import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.Logger
import repositories._

case class ReindexTagsCommand() extends Command {
  override type T = Int

  override def process()(implicit username: Option[String] = None): Option[T] = {
    val reindexJob = Job(
      id = Sequences.jobId.getNextId,
      `type` = "reindexTags",
      started = new DateTime,
      startedBy = username,
      tagIds = List(),
      command = this,
      steps = List(ReindexTags())
    )

    JobRepository.upsertJob(reindexJob)
    SQS.jobQueue.postMessage(reindexJob.id.toString, delaySeconds = 15)

    AppAuditRepository.upsertAppAudit(AppAudit.reindexTags);

    Some(TagLookupCache.allTags.get.count(_ => true))
  }
}

object ReindexTagsCommand {
  implicit val reindexTagsCommandFormat: Format[ReindexTagsCommand] = Jsonx.formatCaseClassUseDefaults[ReindexTagsCommand]
}
