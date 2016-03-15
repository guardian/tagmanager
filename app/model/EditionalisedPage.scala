package model

import org.cvogt.play.json.Jsonx
import com.gu.tagmanagement.{EditionalisedPage => ThriftEditionalisedPage}

case class EditionalisedPage(path: String, pageId: Long) {
  def asThift = ThriftEditionalisedPage(path, pageId)
}

object EditionalisedPage {

  implicit val editionalisedPageFormat = Jsonx.formatCaseClass[EditionalisedPage]

  def apply(thriftEditionalisedPage: ThriftEditionalisedPage): EditionalisedPage =
    EditionalisedPage(thriftEditionalisedPage.path, thriftEditionalisedPage.pageId)
}
