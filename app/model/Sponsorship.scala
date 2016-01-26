package model

import com.amazonaws.services.dynamodbv2.document.Item
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.util.control.NonFatal

case class Sponsorship (
  id: Long,
  validFrom: Option[DateTime],
  validTo: Option[DateTime],
  status: String,
  sponsorshipType: String,
  sponsorName: String,
  sponsorLogo: String,
  sponsorLink: String,
  tag: Option[Long],
  section: Option[Long],
  targetting: Option[SponsorshipTargeting]) {


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
      (JsPath \ "sponsorLogo").format[String] and
      (JsPath \ "sponsorLink").format[String] and
      (JsPath \ "tag").formatNullable[Long] and
      (JsPath \ "section").formatNullable[Long] and
      (JsPath \ "targetting").formatNullable[SponsorshipTargeting]

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