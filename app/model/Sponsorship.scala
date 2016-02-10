package model

import com.amazonaws.services.dynamodbv2.document.Item
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json._
import play.api.libs.functional.syntax._
import repositories.{SectionRepository, TagRepository, TagLookupCache}

import scala.util.control.NonFatal

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
}

object Sponsorship {

  implicit val sponsorshipFormat: Format[Sponsorship] = (
    (JsPath \ "id").format[Long] and
      (JsPath \ "validFrom").formatNullable[DateTime] and
      (JsPath \ "validTo").formatNullable[DateTime] and
      (JsPath \ "status").format[String] and
      (JsPath \ "sponsorshipType").format[String] and
      (JsPath \ "sponsorName").format[String] and
      (JsPath \ "sponsorLogo").format[Image] and
      (JsPath \ "sponsorLink").format[String] and
      (JsPath \ "tag").formatNullable[Long] and
      (JsPath \ "section").formatNullable[Long] and
      (JsPath \ "targeting").formatNullable[SponsorshipTargeting]

    )(Sponsorship.apply, unlift(Sponsorship.unapply))

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

case class SponsorshipTargeting(
  publishedSince: Option[DateTime],
  validGeos: Option[List[String]])

object SponsorshipTargeting {
  implicit val sponsorshipTargettingFormat: Format[SponsorshipTargeting] = (
    (JsPath \ "publishedSince").formatNullable[DateTime] and
      (JsPath \ "validGeos").formatNullable[List[String]]
    )(SponsorshipTargeting.apply, unlift(SponsorshipTargeting.unapply))
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

  implicit val denormalisedSponsorshipFormat: Format[DenormalisedSponsorship] = (
    (JsPath \ "id").format[Long] and
      (JsPath \ "validFrom").formatNullable[DateTime] and
      (JsPath \ "validTo").formatNullable[DateTime] and
      (JsPath \ "status").format[String] and
      (JsPath \ "sponsorshipType").format[String] and
      (JsPath \ "sponsorName").format[String] and
      (JsPath \ "sponsorLogo").format[Image] and
      (JsPath \ "sponsorLink").format[String] and
      (JsPath \ "tag").formatNullable[Tag] and
      (JsPath \ "section").formatNullable[Section] and
      (JsPath \ "targeting").formatNullable[SponsorshipTargeting]

    )(DenormalisedSponsorship.apply, unlift(DenormalisedSponsorship.unapply))

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