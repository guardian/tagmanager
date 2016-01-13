package model.command

import model.AppAudit;
import model.jobs.{Job, ReindexTags}
import services.SQS
import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.Logger
import repositories._

case class ReindexCommand(capiJobId: String) extends Command {
  override type T = Long

  override def process()(implicit username: Option[String] = None): Option[T] = {
    val reindexJob = Job(
      id = Sequences.jobId.getNextId,
      `type` = "reindex",
      started = new DateTime,
      startedBy = username,
      tagIds = List(),
      command = this,
      steps = List(ReindexTags(capiJobId))
    )

    JobRepository.upsertJob(reindexJob)
    SQS.jobQueue.postMessage(reindexJob.id.toString, delaySeconds = 15)

    AppAuditRepository.upsertAppAudit(AppAudit.reindex(capiJobId));

    Some(reindexJob.id)
  }
}

object ReindexCommand {
  //Weird inmapping required because of a "limitation" in the macro system in play. Meaning it doesn't allow for
  //single field case classes to be serialized using the Format
  //http://stackoverflow.com/questions/14754092/how-to-turn-json-to-case-class-when-case-class-has-only-one-field
  implicit val reindexCommandFormat: Format[ReindexCommand] = (
    JsPath \ "capiJobId"
  ).format[String].inmap(id => ReindexCommand(id), (reindexCommand: ReindexCommand) => reindexCommand.capiJobId)
}
