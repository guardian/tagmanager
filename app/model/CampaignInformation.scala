package model

import play.api.libs.json._
import ai.x.play.json.Jsonx
import ai.x.play.json.Encoders.encoder
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

  implicit val trackingFormat: OFormat[CampaignInformation] = Jsonx.formatCaseClass[CampaignInformation]

  def apply(thriftCampaignInformation: ThriftCampaignInformation): CampaignInformation =
    CampaignInformation(
      campaignType = thriftCampaignInformation.campaignType
    )
}
