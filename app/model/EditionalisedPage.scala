package model

import ai.x.play.json.Jsonx
import ai.x.play.json.Encoders.encoder
import com.gu.tagmanagement.{EditionalisedPage => ThriftEditionalisedPage}

case class EditionalisedPage(path: String, pageId: Long) {
  def asThift = ThriftEditionalisedPage(path, pageId)
}

object EditionalisedPage {

  implicit val editionalisedPageFormat = Jsonx.formatCaseClass[EditionalisedPage]

  def apply(thriftEditionalisedPage: ThriftEditionalisedPage): EditionalisedPage =
    EditionalisedPage(thriftEditionalisedPage.path, thriftEditionalisedPage.pageId)
}
