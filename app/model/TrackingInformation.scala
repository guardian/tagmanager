package model

import play.api.libs.json._
import ai.x.play.json.Jsonx
import ai.x.play.json.Encoders.encoder
import com.gu.tagmanagement.{TrackingInformation => ThriftTrackingInformation}

case class TrackingInformation(trackingType: String) {

  def asThrift = ThriftTrackingInformation(
    trackingType = trackingType
  )

  def asExportedXml = {
    <trackingType>{this.trackingType}</trackingType>
  }
}

object TrackingInformation {

  implicit val trackingFormat = Jsonx.formatCaseClass[TrackingInformation]

  def apply(thriftTrackingInformation: ThriftTrackingInformation): TrackingInformation =
    TrackingInformation(
      trackingType = thriftTrackingInformation.trackingType
    )
}


