package model

import play.api.libs.json._
import org.cvogt.play.json.Jsonx
import org.cvogt.play.json.implicits.optionWithNull
import com.gu.tagmanagement.{Image => ThriftImage}


case class Image(imageId: String, assets: List[ImageAsset]) {
  def asThrift = ThriftImage(
    imageId = imageId,
    assets = assets.map(_.asThrift)
  )
  def asExportedXml = {
    <imageId>{this.imageId}</imageId>
    <assets>{this.assets.map(_.asExportedXml)}</assets>
  }
}

object Image {
  implicit val imageFormat = Jsonx.formatCaseClass[Image]

  def apply(thriftImage: ThriftImage): Image = Image(
    imageId = thriftImage.imageId,
    assets = thriftImage.assets.map(ImageAsset(_)).toList
  )
}

