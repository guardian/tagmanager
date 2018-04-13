package model

import play.api.libs.json._
import org.cvogt.play.json.Jsonx
import com.gu.tagmanagement.{CampaignInformation => ThriftCampaignInformation}

case class CampaignInformation(campaignType: String) {

  def asThrift = ThriftCampaignInformation(
      campaignType = campaignType
  )

  def asExportedXml = {
    <campaignType>{this.campaignType}</campaignType>
  }
}

object CampaignInformation {

  implicit val trackingFormat = Jsonx.formatCaseClass[CampaignInformation]

  def apply(thriftCampaignInformation: ThriftCampaignInformation): CampaignInformation =
    CampaignInformation(
      campaignType = thriftCampaignInformation.campaignType
    )
}