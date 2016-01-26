package model.command

import model.AppAudit;
import model.jobs.{Job, ReindexSections}
import services.SQS
import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.Logger
import repositories._

case class ReindexSectionsCommand(capiJobId: String) extends Command {
  override type T = Int

  override def process()(implicit username: Option[String] = None): Option[T] = {
    val reindexJob = Job(
      id = Sequences.jobId.getNextId,
      `type` = "reindexSections",
      started = new DateTime,
      startedBy = username,
      tagIds = List(),
      command = this,
      steps = List(ReindexSections(capiJobId))
    )

    JobRepository.upsertJob(reindexJob)
    SQS.jobQueue.postMessage(reindexJob.id.toString, delaySeconds = 15)

    AppAuditRepository.upsertAppAudit(AppAudit.reindexSections(capiJobId));

    Some(SectionRepository.count)
  }
}

object ReindexSectionsCommand {
  implicit val reindexSectionsCommandFormat: Format[ReindexSectionsCommand] = (
    JsPath \ "capiJobId"
  ).format[String].inmap(id => ReindexSectionsCommand(id), (reindexSectionsCommand: ReindexSectionsCommand) => reindexSectionsCommand.capiJobId)
}
