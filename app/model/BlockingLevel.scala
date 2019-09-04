package model

import enumeratum.EnumEntry.Uppercase
import enumeratum._
import play.api.libs.json._

sealed trait BlockingLevel extends EnumEntry with Uppercase

object BlockingLevel extends Enum[BlockingLevel] {
  override val values = findValues

  case object NONE extends BlockingLevel

  case object SUGGEST extends BlockingLevel

  case object FORCE extends BlockingLevel

  override def withName(name: String): BlockingLevel = {
    super.withName(name.toUpperCase)
  }

  implicit val format: Format[BlockingLevel] = new Format[BlockingLevel] {
    def writes(level: BlockingLevel): JsValue = JsString(level.entryName)

    def reads(json: JsValue): JsResult[BlockingLevel] = Reads.StringReads.reads(json).flatMap(v => {
      val level = BlockingLevel.withName(v)
      level match {
        case BlockingLevel.NONE => JsSuccess(NONE)
        case BlockingLevel.SUGGEST => JsSuccess(SUGGEST)
        case BlockingLevel.FORCE => JsSuccess(FORCE)
      }
    })
  }
}

