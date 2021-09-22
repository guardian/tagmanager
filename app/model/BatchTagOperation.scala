package model

import enumeratum._
import enumeratum.EnumEntry.Hyphencase

sealed trait BatchTagOperation extends EnumEntry with Hyphencase

object BatchTagOperation extends Enum[BatchTagOperation] {
  override val values = findValues
  case object AddToTop extends BatchTagOperation
  case object AddToBottom extends BatchTagOperation
  case object AddTrackingTag extends BatchTagOperation
  case object Remove extends BatchTagOperation
}
