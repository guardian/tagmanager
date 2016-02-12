package repositories

import com.amazonaws.services.dynamodbv2.document.ScanFilter
import model.Sponsorship
import org.joda.time.DateTime
import services.Dynamo

import scala.collection.JavaConversions._


object SponsorshipRepository {
  def getSponsorship(id: Long) = {
    Option(Dynamo.sponsorshipTable.getItem("id", id)).map(Sponsorship.fromItem)
  }

  def updateSponsorship(sponsorship: Sponsorship) = {
    try {
      Dynamo.sponsorshipTable.putItem(sponsorship.toItem)
      Some(sponsorship)
    } catch {
      case e: Error => None
    }
  }

  def loadAllSponsorships = Dynamo.sponsorshipTable.scan().map(Sponsorship.fromItem)

  def searchSponsorships(criteria: SponsorshipSearchCriteria) = {
    val filters = criteria.asFilters
    if (filters.isEmpty) {
      Dynamo.sponsorshipTable.scan().map(Sponsorship.fromItem).toList
    } else {
      Dynamo.sponsorshipTable.scan(filters: _*).map(Sponsorship.fromItem).toList
    }
  }

}

case class SponsorshipSearchCriteria(
  q: Option[String] = None,
  status: Option[String] = None,
  `type`: Option[String] = None) {

  def optionalise(s: Option[String]) = s.map(_.trim) match {
    case Some("") => None
    case o => o
  }

  def asFilters = {
    Seq() ++
      optionalise(q).map{query => new ScanFilter("sponsorName").beginsWith(query)} ++
      typeFilter ++
      statusFilter
  }

  private def typeFilter: Option[ScanFilter] = {
    `type`.flatMap( _ match {
      case "all" => None
      case s => Some(new ScanFilter("sponsorshipType").eq(s))
    })
  }

  private def statusFilter: Option[ScanFilter] = {
    val now = new DateTime()
    status.flatMap( _ match {
      case "all" => None
      case "expiringSoon" => Some(new ScanFilter("validTo").between(now.getMillis, now.plusDays(5).getMillis))
      case "expiredRecently" => Some(new ScanFilter("validTo").between(now.minusDays(5).getMillis, now.getMillis))
      case "launchingSoon" => Some(new ScanFilter("validFrom").between(now.getMillis, now.plusDays(5).getMillis))
      case "launchedRecently" => Some(new ScanFilter("validFrom").between(now.minusDays(5).getMillis, now.getMillis))
      case s => Some(new ScanFilter("status").eq(s))
    })
  }
}