package model.command

import model.Sponsorship
import org.joda.time.{Interval, DateTime}
import repositories.{SponsorshipSearchCriteria, SponsorshipRepository}


class ClashingSponsorshipsFetch(id: Option[Long], tagIds: List[Long], sectionId: Option[Long], validFrom: Option[DateTime], validTo: Option[DateTime], editions: Option[List[String]]) extends Command {

  type T = List[Sponsorship]

  override def process()(implicit username: Option[String] = None): Option[List[Sponsorship]] = {
    val targetedSponsorships = SponsorshipRepository.searchSponsorships(new SponsorshipSearchCriteria(tagIds = tagIds, sectionId = sectionId))

    val checkInterval = new Interval(validFrom.getOrElse(new DateTime().minusYears(500)), validTo.getOrElse(new DateTime().plusYears(500)))

    Some(targetedSponsorships.filter{ s =>
      lazy val timesOverlap = checkInterval overlaps interval(s.validFrom, s.validTo)
      lazy val editionsOverlap = (editions, s.targeting.flatMap(_.validEditions)) match {
        case(Some(eds1), Some(eds2)) => !eds1.intersect(eds2).isEmpty
        case _ => true // one or other sponsorship targets all editions
      }

      timesOverlap && editionsOverlap

    }.filterNot{s => Some(s.id) == id})
  }

  private def interval(from: Option[DateTime], to: Option[DateTime]) = new Interval(from.getOrElse(new DateTime().minusYears(500)), to.getOrElse(new DateTime().plusYears(500)))
}
