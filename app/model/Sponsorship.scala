package model

import com.amazonaws.services.dynamodbv2.document.Item
import ai.x.play.json.Jsonx
import ai.x.play.json.Encoders.encoder
import ai.x.play.json.implicits.optionWithNull
import org.joda.time.DateTime
import play.api.libs.json.JodaWrites._
import play.api.libs.json.JodaReads._
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
  implicit val sponsorshipTargetingFormat: Format[SponsorshipTargeting]  = Jsonx.formatCaseClass[SponsorshipTargeting]
}

case class Sponsorship (
  id: Long,
  validFrom: Option[DateTime],
  validTo: Option[DateTime],
  status: String,
  sponsorshipType: String,
  sponsorName: String,
  sponsorLogo: Image,
  highContrastSponsorLogo: Option[Image],
  sponsorLink: String,
  aboutLink: Option[String],
  tags: Option[List[Long]],
  sections: Option[List[Long]],
  targeting: Option[SponsorshipTargeting]) {


  def toItem = Item.fromJSON(Json.toJson(this).toString())

  def asThrift: ThriftSponsorship = ThriftSponsorship(
    id = id,
    sponsorshipType = SponsorshipType.valueOf(sponsorshipType).get,
    sponsorName = sponsorName,
    sponsorLogo = sponsorLogo.asThrift,
    highContrastSponsorLogo = highContrastSponsorLogo.map(_.asThrift),
    sponsorLink = sponsorLink,
    aboutLink = aboutLink,
    targeting = targeting.map(_.asThrift),
    validFrom = validFrom.map(_.getMillis),
    validTo = validTo.map(_.getMillis)
  )
}

object Sponsorship {

  implicit val sponsorshipFormat: Format[Sponsorship] = Jsonx.formatCaseClass[Sponsorship]

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
                         highContrastSponsorLogo: Option[Image],
                         sponsorLink: String,
                         aboutLink: Option[String],
                         tags: Option[List[Tag]],
                         sections: Option[List[Section]],
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
      highContrastSponsorLogo = s.highContrastSponsorLogo,
      sponsorLink = s.sponsorLink,
      aboutLink = s.aboutLink,
      tags = s.tags.map(_.flatMap(TagLookupCache.getTag)),
      sections = s.sections.map(_.flatMap(SectionRepository.getSection)),
      targeting = s.targeting
    )
  }
}
