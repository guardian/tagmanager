package model.jobs

import model.jobs.steps._
import ai.x.play.json.Jsonx
import ai.x.play.json.Encoders.encoder
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.Logger
import scala.concurrent.duration._
import scala.util.control.NonFatal

trait Step {
  // Inner details
  /** Do work. */
  protected def process

  /** Confirm this check ran successfully */
  protected def check: Boolean

  /** Undo this step */
  protected def rollback

  // Public methods that wrap the status updates
  def processStep() = {
    try {
      beginProcessing
      process
      doneProcessing
    } catch {
      case NonFatal(e) => {
        Logger.error(s"Error thrown during step processing: ${e}")
        processFailed
        throw e // Need to rethrow the exception to inform the job to start a rollback
      }
    }
  }

  def checkStep() = {
    attempts += 1
    if (attempts > Step.retryLimit) {
      checkFailed
      throw TooManyAttempts(s"Too many attempts at checking ${this.`type`}")
    }

    try {
      if (check) {
          checkOk
      }
    } catch {
      case NonFatal(e) => {
        Logger.error(s"Error thrown during step check: ${e}")
        checkFailed
        throw e // Need to rethrow the exception to inform the job to start a rollback
      }
    }
  }

  def rollbackStep() = {
    try {
      if ( stepStatus == StepStatus.processing
        || stepStatus == StepStatus.processed
        || stepStatus == StepStatus.complete
        || stepStatus == StepStatus.failed) {
          rollback
          stepStatus = StepStatus.rolledback
        }
    } catch {
      case NonFatal(e) => stepStatus = StepStatus.rollbackfailed
    }
  }

  /** The amount of time to wait inbetween steps */
  def waitDuration: Option[Duration]

  /** The type of this step - used in serialization */
  val `type`: String

  /** The number of attempts this step has made at checking */
  var attempts: Int

  /** The status of this step: 'ready' to be processed, 'processed', 'complete', or one of the failed states 'rolledback' and 'rollbackfailed'*/
  var stepStatus: String

  /** The current user friendly message for this step, utilizes the vals below. */
  var stepMessage: String

  // User friendly messages to help the user
  val checkingMessage: String
  val failureMessage: String
  val checkFailMessage: String


  // Helpers for setting status metadata
  private def beginProcessing() = {
    stepStatus = StepStatus.processing
    stepMessage = "Processing..."
  }

  private def doneProcessing() = {
    stepStatus = StepStatus.processed
    stepMessage = checkingMessage
  }

  private def processFailed() = {
    stepStatus = StepStatus.failed
    stepMessage = failureMessage
  }

  private def checkOk() = {
    stepStatus = StepStatus.complete
    stepMessage = "Complete"
  }

  private def checkFailed() = {
    stepStatus = StepStatus.failed
    stepMessage = checkFailMessage
  }
}

object Step {

  private val retryLimit = 10000

  // Keep all the serialization stuff in here just so it's in one place
  val addTagToContentFormat      = Jsonx.formatCaseClassUseDefaults[ModifyContentTags]
  val mergeTagForContentFormat   = Jsonx.formatCaseClassUseDefaults[MergeTagForContent]
  val reindexSectionsFormat      = Jsonx.formatCaseClassUseDefaults[ReindexSections]
  val reindexTagsFormat          = Jsonx.formatCaseClassUseDefaults[ReindexTags]
  val reindexPillarsFormat       = Jsonx.formatCaseClassUseDefaults[ReindexPillars]
  val removeTagFormat            = Jsonx.formatCaseClassUseDefaults[RemoveTag]
  val removeTagFromCapiFormat    = Jsonx.formatCaseClassUseDefaults[RemoveTagFromCapi]
  val removeTagFromContentFormat = Jsonx.formatCaseClassUseDefaults[RemoveTagFromContent]
  val removeTagPathFormat        = Jsonx.formatCaseClassUseDefaults[RemoveTagPath]

  val stepWrites = new Writes[Step] {
    override def writes(step: Step): JsValue = {
      step match {
        case s: ModifyContentTags      => addTagToContentFormat.writes(s)
        case s: MergeTagForContent   => mergeTagForContentFormat.writes(s)
        case s: ReindexSections      => reindexSectionsFormat.writes(s)
        case s: ReindexTags          => reindexTagsFormat.writes(s)
        case s: ReindexPillars       => reindexPillarsFormat.writes(s)
        case s: RemoveTag            => removeTagFormat.writes(s)
        case s: RemoveTagFromCapi    => removeTagFromCapiFormat.writes(s)
        case s: RemoveTagFromContent => removeTagFromContentFormat.writes(s)
        case s: RemoveTagPath        => removeTagPathFormat.writes(s)
        case other => {
          Logger.warn(s"Attempted to serialize unknown step type ${other.getClass}")
          throw new UnsupportedOperationException(s"unable to serialize step of type ${other.getClass}")
        }
      }
    }
  }

  val stepReads = new Reads[Step] {
    override def reads(json: JsValue): JsResult[Step] = {
      (json \ "type").get match {
        case JsString(ModifyContentTags.`type`)      => addTagToContentFormat.reads(json)
        case JsString(MergeTagForContent.`type`)   => mergeTagForContentFormat.reads(json)
        case JsString(ReindexSections.`type`)      => reindexSectionsFormat.reads(json)
        case JsString(ReindexTags.`type`)          => reindexTagsFormat.reads(json)
        case JsString(ReindexPillars.`type`)       => reindexPillarsFormat.reads(json)
        case JsString(RemoveTag.`type`)            => removeTagFormat.reads(json)
        case JsString(RemoveTagFromCapi.`type`)    => removeTagFromCapiFormat.reads(json)
        case JsString(RemoveTagFromContent.`type`) => removeTagFromContentFormat.reads(json)
        case JsString(RemoveTagPath.`type`)        => removeTagPathFormat.reads(json)
        case _ => JsError("unexpected step type value")
      }
    }
  }

  implicit val stepFormat = Format(stepReads, stepWrites)
}

// Step status is required so we know which steps require rollback
object StepStatus {
  val ready          = "ready"
  val processing     = "processing"
  val processed      = "processed"
  val complete       = "complete"

  val failed         = "failed"

  val rolledback     = "rolledback"
  val rollbackfailed = "rollbackfailed"
}

case class TooManyAttempts(message: String) extends RuntimeException(message)
