package model

import org.cvogt.play.json.Jsonx
import com.gu.tagmanagement.{ImageAsset => ThriftImageAsset}

case class ImageAsset(imageUrl: String, width: Long, height: Long, mimeType: String) {
  def asThrift = ThriftImageAsset(imageUrl, width, height, mimeType)
  def asExportedXml = {<imageAsset>
    <imageUrl>{this.imageUrl}</imageUrl>
    <width>{this.width}</width>
    <height>{this.height}</height>
    <mimeType>{this.mimeType}</mimeType>
  </imageAsset>}
}

object ImageAsset {
  implicit val imageAssetFormat = Jsonx.formatCaseClass[ImageAsset]

  def apply(thriftImageAsset: ThriftImageAsset): ImageAsset = ImageAsset(
    imageUrl = thriftImageAsset.imageUrl,
    width = thriftImageAsset.width,
    height = thriftImageAsset.height,
    mimeType = thriftImageAsset.mimeType
  )
}
