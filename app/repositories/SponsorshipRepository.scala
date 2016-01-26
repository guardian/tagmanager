package repositories

import model.Sponsorship
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

}
