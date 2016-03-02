package model

import com.amazonaws.services.dynamodbv2.document.Item
import org.cvogt.play.json.Jsonx
import org.cvogt.play.json.implicits.optionWithNull
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json._
import play.api.libs.functional.syntax._
import repositories.{SectionRepository, TagRepository, TagLookupCache}
import com.gu.tagmanagement.{Sponsorship => ThriftSponsorship, SponsorshipTargeting => ThriftSponsorshipTargeting, SponsorshipType}

import scala.util.control.NonFatal


case class SponsorshipTargeting(publishedSince: Option[DateTime], validEditions: Option[List[String]]) {
  def asThrift: ThriftSponsorshipTargeting = ThriftSponsorshipTargeting(
    publishedSince = publishedSince.map(_.getMillis),
    validEditions = validEditions
  )
}

object SponsorshipTargeting {
  implicit val sponsorshipTargettingFormat = Jsonx.formatCaseClass[SponsorshipTargeting]
}

case class Sponsorship (
  id: Long,
  validFrom: Option[DateTime],
  validTo: Option[DateTime],
  status: String,
  sponsorshipType: String,
  sponsorName: String,
  sponsorLogo: Image,
  sponsorLink: String,
  tag: Option[Long],
  section: Option[Long],
  targeting: Option[SponsorshipTargeting]) {


  def toItem = Item.fromJSON(Json.toJson(this).toString())

  def asThrift: ThriftSponsorship = ThriftSponsorship(
    id = id,
    sponsorshipType = SponsorshipType.valueOf(sponsorshipType).get,
    sponsorName = sponsorName,
    sponsorLogo = sponsorLogo.asThrift,
    sponsorLink = sponsorLink,
    targeting = targeting.map(_.asThrift)
  )
}

object Sponsorship {

  implicit val sponsorshipFormat = Jsonx.formatCaseClass[Sponsorship]

  def fromJson(json: JsValue) = json.as[Sponsorship]

  def fromItem(item: Item) = try {
    Json.parse(item.toJSON).as[Sponsorship]
  } catch {
    case NonFatal(e) => {
      Logger.error(s"failed to load sponsorship ${item.toJSON}", e)
      throw e
    }
  }
}


case class DenormalisedSponsorship (
                         id: Long,
                         validFrom: Option[DateTime],
                         validTo: Option[DateTime],
                         status: String,
                         sponsorshipType: String,
                         sponsorName: String,
                         sponsorLogo: Image,
                         sponsorLink: String,
                         tag: Option[Tag],
                         section: Option[Section],
                         targeting: Option[SponsorshipTargeting])

object DenormalisedSponsorship {

  implicit val denormalisedSponsorshipFormat = Jsonx.formatCaseClass[DenormalisedSponsorship]

  def apply(s: Sponsorship): DenormalisedSponsorship = {
    new DenormalisedSponsorship(
      id = s.id,
      validFrom = s.validFrom,
      validTo = s.validTo,
      status = s.status,
      sponsorshipType = s.sponsorshipType,
      sponsorName = s.sponsorName,
      sponsorLogo = s.sponsorLogo,
      sponsorLink = s.sponsorLink,
      tag = s.tag.flatMap(TagLookupCache.getTag(_)),
      section = s.section.flatMap(SectionRepository.getSection(_)),
      targeting = s.targeting
    )
  }
}