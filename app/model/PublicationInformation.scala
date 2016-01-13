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

  def axExportedXml = {
    <mainNewspaperBookSectionId>{this.mainNewspaperBookSectionId.getOrElse("")}</mainNewspaperBookSectionId>
    <newspaperBooks>{this.newspaperBooks}</newspaperBooks>
  }
}

object PublicationInformation {

  implicit val publicationInformationFormat: Format[PublicationInformation] = (
    (JsPath \ "mainNewspaperBookSectionId").formatNullable[Long] and
      (JsPath \ "newspaperBooks").formatNullable[Set[Long]].inmap[Set[Long]](_.getOrElse(Set()), Some(_))
    )(PublicationInformation.apply, unlift(PublicationInformation.unapply))

  def apply(thriftPublicationInformation: ThriftPublicationInformation): PublicationInformation =
    PublicationInformation(
      mainNewspaperBookSectionId =  thriftPublicationInformation.mainNewspaperBookSectionId,
      newspaperBooks =              thriftPublicationInformation.newspaperBooks.toSet
    )
}
