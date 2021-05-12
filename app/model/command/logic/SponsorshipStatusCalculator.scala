package model.command.logic

import org.joda.time.DateTime
import play.api.libs.json.JodaWrites._
import play.api.libs.json.JodaReads._

object SponsorshipStatusCalculator {

  def calculateStatus(validFrom: Option[DateTime], validTo: Option[DateTime]) =  (validFrom, validTo) match {
    case(None, None)                              => "active"
    case(Some(from), None) if from.isBeforeNow    => "active"
    case(Some(from), None)                        => "pending"
    case(None, Some(to)) if to.isBeforeNow        => "expired"
    case(None, Some(to))                          => "active"
    case(Some(from), Some(to)) if from.isAfterNow => "pending"
    case(Some(from), Some(to)) if to.isBeforeNow  => "expired"
    case(_)                                       => "active"
  }

}
