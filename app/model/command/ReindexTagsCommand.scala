package model.command

import model.AppAudit;
import model.jobs.{Job, ReindexTags}
import services.SQS
import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.Logger
import repositories._

case class ReindexTagsCommand(capiJobId: String) extends Command {
  override type T = Int

  override def process()(implicit username: Option[String] = None): Option[T] = {
    val reindexJob = Job(
      id = Sequences.jobId.getNextId,
      `type` = "reindexTags",
      started = new DateTime,
      startedBy = username,
      tagIds = List(),
      command = this,
      steps = List(ReindexTags(capiJobId))
    )

    JobRepository.upsertJob(reindexJob)
    SQS.jobQueue.postMessage(reindexJob.id.toString, delaySeconds = 15)

    AppAuditRepository.upsertAppAudit(AppAudit.reindexTags(capiJobId));

    Some(TagLookupCache.allTags.get.count(_ => true))
  }
}

object ReindexTagsCommand {
  //Weird inmapping required because of a "limitation" in the macro system in play. Meaning it doesn't allow for
  //single field case classes to be serialized using the Format
  //http://stackoverflow.com/questions/14754092/how-to-turn-json-to-case-class-when-case-class-has-only-one-field
  implicit val reindexTagsCommandFormat: Format[ReindexTagsCommand] = (
    JsPath \ "capiJobId"
  ).format[String].inmap(id => ReindexTagsCommand(id), (reindexTagsCommand: ReindexTagsCommand) => reindexTagsCommand.capiJobId)
}
