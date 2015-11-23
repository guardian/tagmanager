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

trait Step {

  /** runs the step and returns the updated state of the step, or None if the step has completed */
  def process: Option[Step]

}

object Step {
  val stepWrites = new Writes[Step] {
    override def writes(c: Step): JsValue = c match {
      case b: BatchTagAddCompleteCheck => BatchTagAddCompleteCheck.batchTagAddCompleteCheck.writes(b).asInstanceOf[JsObject] + ("type", JsString("BatchTagAddCompleteCheck"))
      case b: BatchTagRemoveCompleteCheck => BatchTagRemoveCompleteCheck.batchTagRemoveCompleteCheck.writes(b).asInstanceOf[JsObject] + ("type", JsString("BatchTagRemoveCompleteCheck"))
      case other => {
        Logger.warn(s"unable to serialise step of type ${other.getClass}")
        throw new UnsupportedOperationException(s"unable to serialise step of type ${other.getClass}")
      }
    }
  }

  val stepReads = new Reads[Step] {
    override def reads(json: JsValue): JsResult[Step] = {
      (json \ "type").get match {
        case JsString("BatchTagAddCompleteCheck") => BatchTagAddCompleteCheck.batchTagAddCompleteCheck.reads(json)
        case JsString("BatchTagRemoveCompleteCheck") => BatchTagRemoveCompleteCheck.batchTagRemoveCompleteCheck.reads(json)
        case JsString(other) => JsError(s"unsupported command type $other}")
        case _ => JsError(s"unexpected command type value")
      }
    }
  }

  implicit val commandFormat: Format[Step] = Format(stepReads, stepWrites)
}

case class BatchTagAddCompleteCheck(contentIds: List[String], apiTagId: String, completed: Int = 0) extends Step {
  val count = contentIds.size

  def process = {
    val currentlyCompleted = ContentAPI.countOccurencesOfTagInContents(contentIds, apiTagId)

    if (currentlyCompleted == count) {
      None
    } else {
      Some(copy(completed = currentlyCompleted))
    }
  }
}

object BatchTagAddCompleteCheck {
  implicit val batchTagAddCompleteCheck: Format[BatchTagAddCompleteCheck] = (
    (JsPath \ "contentIds").formatNullable[List[String]].inmap[List[String]](_.getOrElse(Nil), Some(_)) and
      (JsPath \ "apiTagId").format[String] and
      (JsPath \ "completes").format[Int]
    )(BatchTagAddCompleteCheck.apply, unlift(BatchTagAddCompleteCheck.unapply))
}

case class BatchTagRemoveCompleteCheck(contentIds: List[String], apiTagId: String, completed: Int = 0) extends Step {
  val count = contentIds.size

  def process = {
    val currentlyCompleted = count - ContentAPI.countOccurencesOfTagInContents(contentIds, apiTagId)

    if (currentlyCompleted == count) {
      None
    } else {
      Some(copy(completed = currentlyCompleted))
    }
  }
}

object BatchTagRemoveCompleteCheck {
  implicit val batchTagRemoveCompleteCheck: Format[BatchTagRemoveCompleteCheck] = (
    (JsPath \ "contentIds").formatNullable[List[String]].inmap[List[String]](_.getOrElse(Nil), Some(_)) and
      (JsPath \ "apiTagId").format[String] and
      (JsPath \ "completes").format[Int]
    )(BatchTagRemoveCompleteCheck.apply, unlift(BatchTagRemoveCompleteCheck.unapply))
}


