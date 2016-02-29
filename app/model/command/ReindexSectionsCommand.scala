package model.command

import model.AppAudit;
import model.jobs.{Job, ReindexSections}
import services.SQS
import org.cvogt.play.json.Jsonx
import org.joda.time.DateTime
import play.api.libs.json._
import play.api.Logger
import repositories._

case class ReindexSectionsCommand() extends Command {
  override type T = Unit

  override def process()(implicit username: Option[String] = None): Option[T] = {
    val reindexJob = Job(
      id = Sequences.jobId.getNextId,
      `type` = "reindexSections",
      started = new DateTime,
      startedBy = username,
      tagIds = List(),
      command = this,
      steps = List(ReindexSections())
    )

    ReindexProgressRepository.resetSectionReindexProgress(SectionRepository.count)

    JobRepository.upsertJob(reindexJob)
    SQS.jobQueue.postMessage(reindexJob.id.toString, delaySeconds = 5)

    AppAuditRepository.upsertAppAudit(AppAudit.reindexSections);

    Some(())
  }
}

object ReindexSectionsCommand {
  implicit val reindexSectionsCommandFormat: Format[ReindexSectionsCommand] = Jsonx.formatCaseClassUseDefaults[ReindexSectionsCommand]
}
