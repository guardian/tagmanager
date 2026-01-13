package model

import enumeratum.EnumEntry.Uppercase
import enumeratum._
import play.api.libs.json._


sealed trait KeywordType extends EnumEntry with Uppercase

object KeywordType extends Enum[KeywordType] {
  override val values: IndexedSeq[KeywordType] = findValues

  case object PERSON extends KeywordType

  case object ORGANISATION extends KeywordType

  case object EVENT extends KeywordType

  case object WORK_OF_ART_OR_PRODUCT extends KeywordType

  case object PLACE extends KeywordType

  case object OTHER extends KeywordType


  override def withName(name: String): KeywordType = {
    val normalisedName = name.toUpperCase match {
      case "WORKOFARTORPRODUCT" => "WORK_OF_ART_OR_PRODUCT"
      case other => other
    }
    super.withName(normalisedName)
  }

  implicit val format: Format[KeywordType] = new Format[KeywordType] {
    def writes(level: KeywordType): JsValue = JsString(level.entryName)

    def reads(json: JsValue): JsResult[KeywordType] = Reads.StringReads.reads(json).flatMap(v => {
      val keyword = KeywordType.withName(v)
      keyword match {
        case KeywordType.PERSON => JsSuccess(PERSON)
        case KeywordType.ORGANISATION => JsSuccess(ORGANISATION)
        case KeywordType.EVENT => JsSuccess(EVENT)
        case KeywordType.WORK_OF_ART_OR_PRODUCT => JsSuccess(WORK_OF_ART_OR_PRODUCT)
        case KeywordType.PLACE => JsSuccess(PLACE)
        case KeywordType.OTHER => JsSuccess(OTHER)
      }
    })
  }
}

