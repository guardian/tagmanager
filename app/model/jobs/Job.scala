package model.jobs

import com.amazonaws.services.dynamodbv2.document.Item
import play.api.libs.json._
import play.api.libs.functional.syntax._
import ai.x.play.json.Jsonx
import ai.x.play.json.Encoders.encoder
import model.jobs.steps._
import model.{AppAudit, Tag, TagAudit}
import repositories._
import helpers.JodaDateTimeFormat._
import org.joda.time.{DateTime, DateTimeZone}

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

case class Job(
  id: Long, // Useful so users can report job failures
  title: String,
  createdBy: Option[String],
  steps: List[Step], // What are the steps in this job

  tagIds: List[Long] = List(), // List of all the tags associated with this job
  rollbackEnabled: Boolean = false,
  lockedAt: Long = 0,
  ownedBy: Option[String] = None, // Which node current owns this job
  var jobStatus: String = JobStatus.waiting, // Waiting, Owned, Failed, Complete
  var waitUntil: Long = new DateTime(DateTimeZone.UTC).getMillis, // Signal to the runner to wait until a given time before processing
  createdAt: Long = new DateTime().getMillis // Created at in local time
) {

  /** Process the current step of a job
   *  returns a bool which tells the job runner to requeue the job in dynamo
   *  or simply continue processing. */
  def process(implicit ec: ExecutionContext) = {
    steps.find(_.stepStatus != StepStatus.complete).foreach { step =>
      step.stepStatus match {
        case StepStatus.ready => processStep(step)
        case StepStatus.processed => checkStep(step)
        case StepStatus.failed => failJob(step)
        case _ => {}
      }

      waitUntil = new DateTime(DateTimeZone.UTC).getMillis() + step.waitDuration.map(_.toMillis).getOrElse(0L)
    }
  }

  def processStep(step: Step)(implicit ec: ExecutionContext) = {
    try {
      step.processStep
    } catch {
      case NonFatal(e) => {
        jobStatus = JobStatus.failed
      }
    }
  }

  def checkStep(step: Step)(implicit ec: ExecutionContext) = {
    try {
      step.checkStep
    } catch {
      case NonFatal(e) => {
        jobStatus = JobStatus.failed
      }
    }
  }

  def failJob(step: Step) = {
    jobStatus = JobStatus.failed
  }

  def checkIfComplete() = {
    if (steps.find(_.stepStatus != StepStatus.complete).isEmpty){
      jobStatus = JobStatus.complete
    }
  }

  def rollback = {
    if (rollbackEnabled) {
      val revSteps = steps.reverse
      revSteps.foreach(step => step.rollbackStep())
      jobStatus = JobStatus.rolledback
    }
  }

  def toItem = Item.fromJSON(Json.toJson(this).toString())
}

object Job {
  implicit val jobFormat: Format[Job] = Jsonx.formatCaseClassUseDefaults[Job]

  def fromItem(item: Item): Job = try {
      Json.parse(item.toJSON).as[Job]
    } catch {
      case NonFatal(e) => {
        println(e.printStackTrace())
        throw e
    }
  }
}

/** The job status is used to indicate if a job can be picked up off by a node as well as indicating progress
 *  to clients.
 */
object JobStatus {
  /** This job is waiting to be serviced */
  val waiting  = "waiting"

  /** This job is owned by a node */
  val owned    = "owned"

  /** This job is complete */
  val complete = "complete"

  /** This job has failed */
  val failed   = "failed"

  /** This job has been rolled back by a user */
  val rolledback   = "rolledback"
}
