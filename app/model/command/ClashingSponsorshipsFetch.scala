package model.command

import model.Sponsorship
import org.joda.time.{Interval, DateTime}
import repositories.{SponsorshipSearchCriteria, SponsorshipRepository}


class ClashingSponsorshipsFetch(id: Option[Long], tagId: Option[Long], sectionId: Option[Long], validFrom: Option[DateTime], validTo: Option[DateTime]) extends Command {

  type T = List[Sponsorship]

  override def process()(implicit username: Option[String] = None): Option[List[Sponsorship]] = {
    val targetedSponsorships = SponsorshipRepository.searchSponsorships(new SponsorshipSearchCriteria(tagId = tagId, sectionId = sectionId))

    val checkInterval = new Interval(validFrom.getOrElse(new DateTime().minusYears(500)), validTo.getOrElse(new DateTime().plusYears(500)))

    Some(targetedSponsorships.filter{ s => checkInterval overlaps interval(s.validFrom, s.validTo) }.filterNot{s => Some(s.id) == id})
  }

  private def interval(from: Option[DateTime], to: Option[DateTime]) = new Interval(from.getOrElse(new DateTime().minusYears(500)), to.getOrElse(new DateTime().plusYears(500)))
}
