package model

import play.api.libs.json._
import play.api.libs.functional.syntax._
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

  implicit val trackingReads: Reads[TrackingInformation] = (JsPath \ "trackingType").read[String].map(TrackingInformation(_))

  implicit val trackingWrite: Writes[TrackingInformation] = (JsPath \ "trackingType").write[String].contramap(_.trackingType)

  def apply(thriftTrackingInformation: ThriftTrackingInformation): TrackingInformation =
    TrackingInformation(
      trackingType = thriftTrackingInformation.trackingType
    )
}


