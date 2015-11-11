package model

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Format}
import com.gu.tagmanagement.{EditionalisedPage => ThriftEditionalisedPage}

case class EditionalisedPage(path: String, pageId: Long) {
  def asThift = ThriftEditionalisedPage(path, pageId)
}

object EditionalisedPage {

  implicit val editionalisedPageFormat: Format[EditionalisedPage] = (
      (JsPath \ "path").format[String] and
      (JsPath \ "pageId").format[Long]
    )(EditionalisedPage.apply, unlift(EditionalisedPage.unapply))

  def apply(thriftEditionalisedPage: ThriftEditionalisedPage): EditionalisedPage =
    EditionalisedPage(thriftEditionalisedPage.path, thriftEditionalisedPage.pageId)
}
