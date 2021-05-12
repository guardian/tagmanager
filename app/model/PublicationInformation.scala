package model

import play.api.libs.json._
import ai.x.play.json.Jsonx
import ai.x.play.json.Encoders.encoder
import ai.x.play.json.implicits.optionWithNull
import com.gu.tagmanagement.{PublicationInformation => ThriftPublicationInformation}

case class PublicationInformation(
                                   mainNewspaperBookSectionId: Option[Long],
                                   newspaperBooks: Set[Long]
                                  ) {

  def asThrift = ThriftPublicationInformation(
    mainNewspaperBookSectionId = mainNewspaperBookSectionId,
    newspaperBooks =             newspaperBooks
  )

  def asExportedXml = {
    <mainNewspaperBookSectionId>{this.mainNewspaperBookSectionId.getOrElse("")}</mainNewspaperBookSectionId>
    <newspaperBooks>{this.newspaperBooks}</newspaperBooks>
  }
}

object PublicationInformation {

  implicit val publicationInformationFormat = Jsonx.formatCaseClassUseDefaults[PublicationInformation]

  def apply(thriftPublicationInformation: ThriftPublicationInformation): PublicationInformation =
    PublicationInformation(
      mainNewspaperBookSectionId =  thriftPublicationInformation.mainNewspaperBookSectionId,
      newspaperBooks =              thriftPublicationInformation.newspaperBooks.toSet
    )
}
