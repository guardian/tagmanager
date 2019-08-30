package model

import enumeratum._
import enumeratum.EnumEntry.UpperSnakecase
import play.api.libs.json._

sealed trait BlockingLevel extends EnumEntry with UpperSnakecase

object BlockingLevel extends Enum[BlockingLevel] {
  override val values = findValues

  case object None extends BlockingLevel
  case object Suggest extends BlockingLevel
  case object Force extends BlockingLevel

  implicit val format: Format[BlockingLevel] = new Format[BlockingLevel] {
    def writes(level: BlockingLevel): JsValue = JsString(level.entryName)
    def reads(json: JsValue): JsResult[BlockingLevel] = Reads.StringReads.reads(json).flatMap {
      case "NONE" => JsSuccess(None)
      case "SUGGEST" => JsSuccess(Suggest)
      case "FORCE" => JsSuccess(Force)
      case unknown: String => JsError(s"Invalid ad blocking level: $unknown")
    }
  }
}

