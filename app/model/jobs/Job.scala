package model.jobs

import com.amazonaws.services.dynamodbv2.document.Item
import play.api.libs.json._
import play.api.libs.functional.syntax._
import org.cvogt.play.json.Jsonx
import model.jobs.steps._
import model.{AppAudit, Tag, TagAudit}
import repositories._
import scala.util.control.NonFatal

case class Job(
  id: Long, // Useful so users can report job failures
  createdAt: Long, // Created at in local time
  `type`: JobType.Value, // The kind of job this (merge, delete, reindex, etc.)
  status: JobStatus.Value, // Waiting, Owned, Failed, Complete
  owner: Option[String], // Which node current owns this job
  steps: List[Step], // What are the steps in this job
  var retries: Int, // How many times have we had to retry a check
  waitUntil: Long // Signal to the runner to wait until a given time before processing
) {

  val retryLimit = 10 // TODO tune

  /** Process the current step of a job
   *  returns a bool which tells the job runner to requeue the job in dynamo
   *  or simply continue processing. */
  def processStep() = {
    steps.find(_.status != StepStatus.complete).map { step =>
      step.status match {

        case StepStatus.ready => {
          retries = 0
          step.process
          step.status = StepStatus.processed
        }

        case StepStatus.processed => {
          if (retries >= retryLimit) {
            throw new TooManyAttempts("Took too many attempts to process step")
          }

          if (step.check) {
            step.status = StepStatus.complete
          } else {
            retries = retries + 1
          }
        }
        case _ => {}
      }
    }
  }

  def rollback = {
    // Undo in reverse order
    val revSteps = steps.reverse
    revSteps
      .filter(s => s.status == StepStatus.complete || s.status == StepStatus.processed)
      .foreach(step => {
        try {
          step.rollback
          step.status = StepStatus.rolledback
        } catch {
          case NonFatal(e) => step.status = StepStatus.rollbackfailed
        }
      })
  }

  def toItem = Item.fromJSON(Json.toJson(this).toString())
}

// Helpers to lauch jobs and to associated functions such as add audits
object Job {
  implicit val jobFormat: Format[Job] = Jsonx.formatCaseClassUseDefaults[Job]

  def fromItem(item: Item): Job = {
    Json.parse(item.toJSON).as[Job]
  }
}

/** Describes the type of job. What it does. */
object JobType extends Enumeration {
  val delete          = Value("delete")
  val merge           = Value("merge")
  val reindexTags     = Value("reindex-tags")
  val reindexSections = Value("reindex-sections")
  implicit val jobTypeFormat: Format[JobType.Value] = Jsonx.formatAuto[JobType.Value]
}

/** The job status is used to indicate if a job can be picked up off by a node as well as indicating progress
 *  to clients.
 */
object JobStatus extends Enumeration {
  /** This job is waiting to be serviced */
  val waiting  = Value("waiting")

  /** This job is owned by a node */
  val owned    = Value("owned")

  /** This job is complete */
  val complete = Value("complete")

  /** This job has failed */
  val failed   = Value("failed")

  implicit val jobStatusFormat: Format[JobStatus.Value] = Jsonx.formatAuto[JobStatus.Value]
}

case class TooManyAttempts(message: String) extends RuntimeException(message)
