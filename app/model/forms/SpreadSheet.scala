package model.forms

import play.api.libs.json._
import enumeratum._
import enumeratum.EnumEntry.{ Uncapitalised}

sealed trait FilterTypes extends EnumEntry with Uncapitalised

object FilterTypes extends Enum[FilterTypes] {
  override val values = findValues

  case object Path extends FilterTypes
  case object InternalName extends FilterTypes
  case object ExternalName extends FilterTypes
  case object Type extends FilterTypes
  case object HasFields extends FilterTypes
  case object Description extends FilterTypes
}

case class SpreadSheetFilter(`type`: FilterTypes, value: String)

case class GetSpreadSheet(filters: List[SpreadSheetFilter])

object GetSpreadSheet {
  implicit val filterTypesRead: Reads[FilterTypes] = new Reads[FilterTypes] {
    def reads(json: JsValue): JsResult[FilterTypes] = Reads.StringReads.reads(json).flatMap {
      case "hasFields" => JsSuccess(FilterTypes.HasFields)
      case "path" => JsSuccess(FilterTypes.Path)
      case "internalName" => JsSuccess(FilterTypes.InternalName)
      case "externalName" => JsSuccess(FilterTypes.ExternalName)
      case "type" => JsSuccess(FilterTypes.Type)
      case "description" => JsSuccess(FilterTypes.Description)
      case unknown: String => JsError(s"Invalid filter type: $unknown")
    }
  }

  implicit val filterFormat: Reads[SpreadSheetFilter] = Json.reads[SpreadSheetFilter]
  implicit val format: Reads[GetSpreadSheet] = Json.reads[GetSpreadSheet]
}
