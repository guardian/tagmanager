package model

import play.api.libs.json._
import play.api.libs.functional.syntax._
import com.gu.tagmanagement.{ContributorInformation => ThriftContributorInformation}

case class ContributorInformation(
                                   rcsId: Option[String],
                                   bylineImage: Option[Image],
                                   largeBylineImage: Option[Image],
                                   twitterHandle: Option[String],
                                   contactEmail: Option[String]
) {

  def asThrift = ThriftContributorInformation(
    rcsId =             rcsId,
    bylineImage =       bylineImage.map(_.asThrift),
    largeBylineImage =  largeBylineImage.map(_.asThrift),
    twitterHandle =     twitterHandle,
    contactEmail =      contactEmail
  )
}

object ContributorInformation {

  implicit val contributorInformationFormat: Format[ContributorInformation] = (
    (JsPath \ "rcsId").formatNullable[String] and
      (JsPath \ "bylineImage").formatNullable[Image] and
      (JsPath \ "largeBylineImage").formatNullable[Image] and
      (JsPath \ "twitterHandle").formatNullable[String] and
      (JsPath \ "contactEmail").formatNullable[String]
    )(ContributorInformation.apply, unlift(ContributorInformation.unapply))

  def apply(thriftContributorInformation: ThriftContributorInformation): ContributorInformation =
    ContributorInformation(
      rcsId =             thriftContributorInformation.rcsId,
      bylineImage =       thriftContributorInformation.bylineImage.map(Image(_)),
      largeBylineImage =  thriftContributorInformation.largeBylineImage.map(Image(_)),
      twitterHandle =     thriftContributorInformation.twitterHandle,
      contactEmail =      thriftContributorInformation.contactEmail
    )
}

