package model

import play.api.libs.json._
import play.api.libs.functional.syntax._
import com.gu.tagmanagement.{PublicationInformation => ThriftPublicationInformation}

case class PublicationInformation(
                                   mainNewspaperBookSectionId: Option[Long],
                                   newspaperBooks: Set[Long]
                                  ) {

  def asThrift = ThriftPublicationInformation(
    mainNewspaperBookSectionId = mainNewspaperBookSectionId,
    newspaperBooks =             newspaperBooks
  )

  def asXml = {
    <mainNewspaperBookSectionId>{this.mainNewspaperBookSectionId.getOrElse("")}</mainNewspaperBookSectionId>
      <newspaperBooks>{this.newspaperBooks.getOrElse("")}</newspaperBooks>
  }
}

object PublicationInformation {

  implicit val publicationInformationFormat: Format[PodcastMetadata] = (
    (JsPath \ "mainNewspaperBookSectionId").formatNullable[Long] and
      (JsPath \ "newspaperBooks").formatNullable[Seq[Long]].inmap[Seq[Long]](_.getOrElse(Seq()), Some(_))
    )(PublicationInformation.apply, unlift(PublicationInformation.unapply))

  def apply(thriftPublicationInformation: ThriftPublicationInformation): PublicationInformation =
    PublicationInformation(
      mainNewspaperBookSectionId =  thriftPublicationInformation.mainNewspaperBookSectionId,
      newspaperBooks =              thriftPublicationInformation.newspaperBooks
    )
}
