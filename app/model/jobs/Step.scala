package model.jobs

import play.api.Logger
import play.api.libs.json._


trait Step {

  /** runs the step and returns the updated state of the step, or None if the step has completed */
  def process: Option[Step]

}

object Step {
  val stepWrites = new Writes[Step] {
    override def writes(c: Step): JsValue = c match {
      case b: BatchTagAddCompleteCheck => BatchTagAddCompleteCheck.batchTagAddCompleteCheck.writes(b).asInstanceOf[JsObject] + ("type", JsString("BatchTagAddCompleteCheck"))
      case b: BatchTagRemoveCompleteCheck => BatchTagRemoveCompleteCheck.batchTagRemoveCompleteCheck.writes(b).asInstanceOf[JsObject] + ("type", JsString("BatchTagRemoveCompleteCheck"))
      case b: RemoveTagStep => RemoveTagStep.removeTagStepFormat.writes(b).asInstanceOf[JsObject] + ("type", JsString("RemoveTagStep"))
      case b: AllUsagesOfTagRemovedCheck => AllUsagesOfTagRemovedCheck.allUsagesOfTagRemovedCheckFormat.writes(b).asInstanceOf[JsObject] + ("type", JsString("AllUsagesOfTagRemovedCheck"))
      case trc: TagRemovedCheck => JsObject(Map("type" -> JsString("TagRemovedCheck"), "apiTagId" -> JsString(trc.apiTagId)))
      case mas: MergeAuditStep => MergeAuditStep.mergeAuditStepFormat.writes(mas).asInstanceOf[JsObject] + ("type" -> JsString("MergeAuditStep"))
      case ridxtags: ReindexTags => ReindexTags.reindexTagsFormat.writes(ridxtags).asInstanceOf[JsObject] + ("type", JsString("ReindexTags"))
      case ridxsections: ReindexSections => ReindexSections.reindexSectionsFormat.writes(ridxsections).asInstanceOf[JsObject] + ("type", JsString("ReindexSectionss"))

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
        case JsString("RemoveTagStep") => RemoveTagStep.removeTagStepFormat.reads(json)
        case JsString("AllUsagesOfTagRemovedCheck") => AllUsagesOfTagRemovedCheck.allUsagesOfTagRemovedCheckFormat.reads(json)
        case JsString("TagRemovedCheck") => (json \ "apiTagId").validate[String].map(TagRemovedCheck)
        case JsString("MergeAuditStep") => MergeAuditStep.mergeAuditStepFormat.reads(json)
        case JsString("ReindexTags") => ReindexTags.reindexTagsFormat.reads(json)
        case JsString("ReindexSections") => ReindexSections.reindexSectionsFormat.reads(json)
        case JsString(other) => JsError(s"unsupported command type $other}")
        case _ => JsError(s"unexpected command type value")
      }
    }
  }

  implicit val commandFormat: Format[Step] = Format(stepReads, stepWrites)
}
