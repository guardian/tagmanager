package model

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Format}

case class EditionalisedPage(path: String, pageId: Long)

object EditionalisedPage {

  implicit val editionalisedPageFormat: Format[EditionalisedPage] = (
      (JsPath \ "path").format[String] and
      (JsPath \ "pageId").format[Long]
    )(EditionalisedPage.apply, unlift(EditionalisedPage.unapply))
}
