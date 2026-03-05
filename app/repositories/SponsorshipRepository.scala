package repositories

import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument
import model.Sponsorship
import org.joda.time.DateTime
import services.Dynamo

import scala.jdk.CollectionConverters._


object SponsorshipRepository {
  def getSponsorship(id: Long) = {
    Dynamo.sponsorshipTable.getItemConsistent("id", id).map(Sponsorship.fromItem)
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
    val all = loadAllSponsorships.toList
    criteria.filter(all)
  }

  def getSponsorshipsToActivate: List[Sponsorship] = {
    val now = new DateTime().getMillis
    loadAllSponsorships.filter { s =>
      s.status != "active" && (
        // Has start and end, and we're in between
        (s.validFrom.exists(_.getMillis < now) && s.validTo.exists(_.getMillis > now)) ||
        // Has start but no end
        (s.validFrom.exists(_.getMillis < now) && s.validTo.isEmpty) ||
        // No start but has end that hasn't passed
        (s.validFrom.isEmpty && s.validTo.exists(_.getMillis > now)) ||
        // No start and no end (always active)
        (s.validFrom.isEmpty && s.validTo.isEmpty)
      )
    }.toList
  }

  def getSponsorshipsToExpire: List[Sponsorship] = {
    val now = new DateTime().getMillis
    loadAllSponsorships.filter { s =>
      s.status == "active" && s.validTo.exists(_.getMillis < now)
    }.toList
  }
}

case class SponsorshipSearchCriteria(
  q: Option[String] = None,
  status: Option[String] = None,
  `type`: Option[String] = None,
  tagId: Option[Long] = None,
  sectionId: Option[Long] = None
) {

  def filter(sponsorships: List[Sponsorship]): List[Sponsorship] = {
    sponsorships
      .filter(s => q.forall(query => s.sponsorName.toLowerCase.startsWith(query.toLowerCase.trim)))
      .filter(s => typeFilter(s))
      .filter(s => statusFilter(s))
      .filter(s => tagId.forall(id => s.tags.exists(_.contains(id))))
      .filter(s => sectionId.forall(id => s.sections.exists(_.contains(id))))
  }

  private def typeFilter(s: Sponsorship): Boolean = {
    `type` match {
      case None | Some("all") => true
      case Some(t) => s.sponsorshipType == t
    }
  }

  private def statusFilter(s: Sponsorship): Boolean = {
    val now = new DateTime()
    status match {
      case None | Some("all") => true
      case Some("expiringSoon") =>
        s.validTo.exists(v => v.getMillis >= now.getMillis && v.getMillis <= now.plusDays(5).getMillis)
      case Some("expiredRecently") =>
        s.validTo.exists(v => v.getMillis >= now.minusDays(5).getMillis && v.getMillis <= now.getMillis)
      case Some("launchingSoon") =>
        s.validFrom.exists(v => v.getMillis >= now.getMillis && v.getMillis <= now.plusDays(5).getMillis)
      case Some("launchedRecently") =>
        s.validFrom.exists(v => v.getMillis >= now.minusDays(5).getMillis && v.getMillis <= now.getMillis)
      case Some(st) => s.status == st
    }
  }
}
