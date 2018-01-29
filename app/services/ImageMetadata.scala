package services

import java.io.{File, ByteArrayInputStream}
import javax.imageio.ImageIO

import java.security.MessageDigest
import play.api.Logger

import okhttp3.Request.Builder
import okhttp3.OkHttpClient
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Format}

case class ImageMetadataFetchFail(m: String) extends RuntimeException(m)

case class ImageMetadata(width: Int, height: Int, size: Int, mediaId: String, mimeType: String)

object ImageMetadata {
  implicit val imageMetadataFormat: Format[ImageMetadata] = (
    (JsPath \ "width").format[Int] and
      (JsPath \ "height").format[Int] and
      (JsPath \ "size").format[Int] and
      (JsPath \ "id").format[String] and
      (JsPath \ "mimeType").format[String]
    )(ImageMetadata.apply, unlift(ImageMetadata.unapply))
}


object ImageMetadataService {

  val httpClient = new OkHttpClient()

  def calculateMediaId(imageBytes: Array[Byte]): String = {
    // Replicated from Media Service logic at
    // https://github.com/guardian/media-service/blob/master/image-loader/app/lib/play/BodyParsers.scala#L16
    val digest = MessageDigest.getInstance("SHA-1").digest(imageBytes)
    digest.map("%02x".format(_)).mkString
  }


  def fetch(uri: String): Option[ImageMetadata] = {

    val imageRequest = new Builder().url(uri).build()
    val response = httpClient.newCall(imageRequest).execute()

    response.code match {
      case 200 => {

        val mimeType = response.header("content-type")
        val bytes = response.body().bytes()
        val mediaId = calculateMediaId(bytes)
        val image = ImageIO.read(new ByteArrayInputStream(bytes))

        Some(ImageMetadata(image.getWidth, image.getHeight, bytes.size, mediaId, mimeType))
      }
      case c => {
        val body = response.body
        Logger.warn(s"failed to get image metadata for $uri. response code $c, message $body")
        throw ImageMetadataFetchFail(s"failed to get image metadata for $uri. response code $c, message $body")
      }
    }
  }

  def imageDimensions(imageFile: File) = {
    val image = ImageIO.read(imageFile)
    (image.getWidth, image.getHeight)
  }

  def imageHeight(imageFile: File): Long = {
    val image = ImageIO.read(imageFile)
    image.getHeight
  }

  def imageWidth(imageFile: File): Long = {
    val image = ImageIO.read(imageFile)
    image.getWidth
  }
}
