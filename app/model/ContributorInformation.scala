package model

import play.api.libs.json._
import org.cvogt.play.json.Jsonx
import org.cvogt.play.json.implicits.optionWithNull
import com.gu.tagmanagement.{ContributorInformation => ThriftContributorInformation}

case class ContributorInformation(
                                   rcsId: Option[String],
                                   bylineImage: Option[Image],
                                   largeBylineImage: Option[Image],
                                   twitterHandle: Option[String],
                                   contactEmail: Option[String],
                                   firstName: Option[String],
                                   lastName: Option[String]
) {

  def asThrift = ThriftContributorInformation(
    rcsId =             rcsId,
    bylineImage =       bylineImage.map(_.asThrift),
    largeBylineImage =  largeBylineImage.map(_.asThrift),
    twitterHandle =     twitterHandle,
    contactEmail =      contactEmail,
    firstName =         firstName,
    lastName =          lastName
  )
  def asExportedXml = {
    <rcsId>{this.rcsId.getOrElse("")}</rcsId>
    <bylineImage>{this.bylineImage.map(_.asExportedXml).getOrElse("")}</bylineImage>
    <largeBylineImage>{this.largeBylineImage.map(_.asExportedXml).getOrElse("")}</largeBylineImage>
    <twitterHandle>{this.twitterHandle.getOrElse("")}</twitterHandle>
    <contactEmail>{this.contactEmail.getOrElse("")}</contactEmail>
  }
}

object ContributorInformation {

  implicit val contributorInformationFormat = Jsonx.formatCaseClass[ContributorInformation]

  def apply(thriftContributorInformation: ThriftContributorInformation): ContributorInformation =
    ContributorInformation(
      rcsId =             thriftContributorInformation.rcsId,
      bylineImage =       thriftContributorInformation.bylineImage.map(Image(_)),
      largeBylineImage =  thriftContributorInformation.largeBylineImage.map(Image(_)),
      twitterHandle =     thriftContributorInformation.twitterHandle,
      contactEmail =      thriftContributorInformation.contactEmail,
      firstName =         thriftContributorInformation.firstName,
      lastName =          thriftContributorInformation.lastName
    )
}
