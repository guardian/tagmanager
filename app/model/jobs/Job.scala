package model.jobs

import com.amazonaws.services.dynamodbv2.document.Item
import model.command.Command
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json._
import repositories.JobRepository

import scala.util.control.NonFatal

case class Job(
  id: Long,
  `type`: String,
  started: DateTime,
  startedBy: Option[String],
  tagIds: List[Long],
  command: Command,
  steps: List[Step]) {

  def toItem = Item.fromJSON(Json.toJson(this).toString())
}

object Job {
  implicit val batchTagCommandFormat: Format[Job] = (
      (JsPath \ "id").format[Long] and
      (JsPath \ "type").format[String] and
      (JsPath \ "started").format[Long].inmap[DateTime](new DateTime(_), _.getMillis) and
      (JsPath \ "startedBy").formatNullable[String] and
      (JsPath \ "tagIds").formatNullable[List[Long]].inmap[List[Long]](_.getOrElse(Nil), Some(_)) and
      (JsPath \ "command").format[Command] and
      (JsPath \ "steps").formatNullable[List[Step]].inmap[List[Step]](_.getOrElse(Nil), Some(_))
    )(Job.apply, unlift(Job.unapply))


  def fromItem(item: Item) = try {
    Json.parse(item.toJSON).as[Job]
  } catch {
    case NonFatal(e) => {
      Logger.error(s"failed to load job ${item.toJSON}", e)
      throw e
    }
  }
}

object JobRunner {

  /** runs the job up to the first incomplete step, returns the updated job state or None if the job is complete */
  def run(job: Job): Option[Job] = {
    job.steps match {
      case Nil => {
        Logger.info(s"job $job complete")
        JobRepository.deleteJob(job.id)
        None
      }
      case s :: ss => {
        s.process match {
          case Some(updatedStep) => {
            Logger.info(s"job $job step $s not yet complete, updating state")
            val updatedJob = job.copy(steps = updatedStep :: ss)
            JobRepository.upsertJob(updatedJob)
            Some(updatedJob)
          }
          case None => {
            Logger.info(s"job $job step $s complete, processing next step")
            run(job.copy(steps = ss))
          }
        }
      }
    }
  }
}


