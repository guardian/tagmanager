package model.jobs

import model.command.Command
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json._
import repositories.ContentAPI

case class Job(id: Long, `type`: String, started: DateTime, startedBy: Option[String], command: Command, steps: List[Step]) {

}

object Job {
  implicit val batchTagCommandFormat: Format[Job] = (
      (JsPath \ "id").format[Long] and
      (JsPath \ "type").format[String] and
      (JsPath \ "started").format[Long].inmap[DateTime](new DateTime(_), _.getMillis) and
      (JsPath \ "startedBy").formatNullable[String] and
      (JsPath \ "command").format[Command] and
      (JsPath \ "steps").formatNullable[List[Step]].inmap[List[Step]](_.getOrElse(Nil), Some(_))
    )(Job.apply, unlift(Job.unapply))
}

object JobRunner {

  /** runs the job up to the first incomplete step, returns the updated job state or None if the job is complete */
  def run(job: Job): Option[Job] = {
    job.steps match {
      case Nil => {
        Logger.info(s"job $job complete")
        // delete job from DB
        None
      }
      case s :: ss => {
        s.process match {
          case Some(updatedStep) => {
            val updatedJob = job.copy(steps = updatedStep :: ss)
            // persist job state
            Some(job.copy(steps = updatedStep :: ss))
          }
          case None => run(job.copy(steps = ss))
        }
      }
    }
  }
}


