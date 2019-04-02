package model

import enumeratum._
import enumeratum.EnumEntry.UpperSnakecase
import play.api.libs.json._

sealed trait AdBlockingLevel extends EnumEntry with UpperSnakecase

object AdBlockingLevel extends Enum[AdBlockingLevel] {
  override val values = findValues

  case object None extends AdBlockingLevel
  case object Suggest extends AdBlockingLevel
  case object Force extends AdBlockingLevel


  implicit val format: Format[AdBlockingLevel] = new Format[AdBlockingLevel] {
    def writes(level: AdBlockingLevel): JsValue = JsString(level.entryName)
    def reads(json: JsValue): JsResult[AdBlockingLevel] = Reads.StringReads.reads(json).flatMap {
      case "NONE" => JsSuccess(None)
      case "SUGGEST" => JsSuccess(Suggest)
      case "FORCE" => JsSuccess(Force)
      case unknown: String => JsError(s"Invalid ad blocking level: $unknown")
    }
  }
}

