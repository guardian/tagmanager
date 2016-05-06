package model

import org.cvogt.play.json.Jsonx
import com.gu.tagmanagement.{PaidContentInformation => ThriftPaidContentInformation}

case class PaidContentInformation(paidContentType: String) {

  def asThrift = ThriftPaidContentInformation(
    paidContentType = paidContentType
  )

  def asExportedXml = {
    <paidContentType>{this.paidContentType}</paidContentType>
  }
}

object PaidContentInformation {

  implicit val paidContentInformationFormat = Jsonx.formatCaseClass[PaidContentInformation]

  def apply(thriftPaidContentInformation: ThriftPaidContentInformation): PaidContentInformation =
    PaidContentInformation(
      paidContentType = thriftPaidContentInformation.paidContentType
    )
}
