package model.jobs

import model.jobs.steps._
import org.cvogt.play.json.Jsonx
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.Logger
import scala.concurrent.duration._

trait Step {
  /** Do work. */
  def process

  /** The amount of time to wait inbetween steps */
  def waitDuration: Option[Duration]

  /** Confirm this check ran successfully */
  def check: Boolean

  /** Undo this step */
  def rollback

  /** Audit this step */
  def audit

  /** What to display to a user if this step fails */
  def failureMessage: String

  /** The status of this step: 'ready' to be processed, 'processed', 'complete', or one of the failed states 'rolledback' and 'rollbackfailed'*/
  var status = StepStatus.ready

  /** The type of this step */
  val `type`: String
}

object Step {
  // Keep all the serialization stuff in here just so it's in one place
  val addTagToContentFormat      = Jsonx.formatCaseClassUseDefaults[AddTagToContent]
  val mergeTagForContentFormat   = Jsonx.formatCaseClassUseDefaults[MergeTagForContent]
  val reindexSectionsFormat      = Jsonx.formatCaseClassUseDefaults[ReindexSections]
  val reindexTagsFormat          = Jsonx.formatCaseClassUseDefaults[ReindexTags]
  val removeTagFormat            = Jsonx.formatCaseClassUseDefaults[RemoveTag]
  val removeTagFromCapiFormat    = Jsonx.formatCaseClassUseDefaults[RemoveTagFromCapi]
  val removeTagFromContentFormat = Jsonx.formatCaseClassUseDefaults[RemoveTagFromContent]
  val removeTagPathFormat        = Jsonx.formatCaseClassUseDefaults[RemoveTagPath]

  val stepWrites = new Writes[Step] {
    override def writes(step: Step): JsValue = step match {
      case s: AddTagToContent      => addTagToContentFormat.writes(s)
      case s: MergeTagForContent   => mergeTagForContentFormat.writes(s)
      case s: ReindexSections      => reindexSectionsFormat.writes(s)
      case s: ReindexTags          => reindexTagsFormat.writes(s)
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

  val stepReads = new Reads[Step] {
    override def reads(json: JsValue): JsResult[Step] = {
      (json \ "type").get match {
        case JsString(AddTagToContent.`type`)      => addTagToContentFormat.reads(json)
        case JsString(MergeTagForContent.`type`)   => mergeTagForContentFormat.reads(json)
        case JsString(ReindexSections.`type`)      => reindexSectionsFormat.reads(json)
        case JsString(ReindexTags.`type`)          => reindexTagsFormat.reads(json)
        case JsString(RemoveTag.`type`)            => removeTagFormat.reads(json)
        case JsString(RemoveTagFromCapi.`type`)    => removeTagFromCapiFormat.reads(json)
        case JsString(RemoveTagFromContent.`type`) => removeTagFromContentFormat.reads(json)
        case JsString(RemoveTagPath.`type`)        => removeTagPathFormat.reads(json)
        case _ => JsError("unexpeted step type value")
      }
    }
  }

  implicit val stepFormat = Format(stepReads, stepWrites)
}

// Step status is required so we know which steps require rollback
object StepStatus extends Enumeration {
  val ready          = Value("ready")
  val processed      = Value("processed")
  val complete       = Value("complete")
  val rolledback     = Value("rolledback")
  val rollbackfailed = Value("rollbackfailed")
}
