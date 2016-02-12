package controllers

import com.amazonaws.services.s3.model._
import model.{ImageAsset, Image}
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.{AWS, Config, ImageMetadataService}
import com.squareup.okhttp.{Request, OkHttpClient, Credentials}
import java.util.concurrent.TimeUnit

object Support extends Controller with PanDomainAuthActions {

  def imageMetadata(imageUrl: String = "") = APIAuthAction {
    ImageMetadataService.fetch(imageUrl).map { imageMetadata =>
      Ok(Json.toJson(imageMetadata))
    }.getOrElse(NotFound)
  }

  def uploadLogo(filename: String) = APIAuthAction(parse.temporaryFile) { req =>
    val picture = req.body

    ImageMetadataService.imageDimensions(picture.file) match {
      case (w, h) if w <= 140 && h <= 90 => {
        val dateSlug = new DateTime().toString("dd/MMM/yyyy")
        val logoPath = s"commercial/sponsor/${dateSlug}/${filename}"
        val contentType = req.contentType
        val objectMetadata = new ObjectMetadata()
        contentType.foreach(objectMetadata.setContentType(_))

        AWS.S3Client.putObject(
          new PutObjectRequest("static-theguardian-com", logoPath, picture.file)
            // .withAccessControlList(acl)
            .withCannedAcl(CannedAccessControlList.PublicRead)
            .withMetadata(objectMetadata)
        )

        val uploadedImageUrl = s"https://static.theguardian.com/${logoPath}"

        ImageMetadataService.fetch(uploadedImageUrl).map { imageMetadata =>
          val image = Image(imageMetadata.mediaId, List(
            ImageAsset(
              imageUrl = uploadedImageUrl,
              width = imageMetadata.width,
              height = imageMetadata.height,
              mimeType = imageMetadata.mimeType
            )
          ))
          Ok(Json.toJson(image))
        }.getOrElse(BadRequest("failed to upload image"))
      }
      case _ => BadRequest("sponsorship logos must be at most 140 x 90")
    }
  }

  def previewCapiProxy(path: String) = APIAuthAction { request =>
    val httpClient = new OkHttpClient()

    val url = s"${Config().capiPreviewUrl}/${path}?${request.rawQueryString}"
    Logger.info(s"Requesting CAPI preview -> ${url}")

    val req = new Request.Builder()
      .url(url)
      .header("Authorization", Credentials.basic(Config().capiPreviewUser, Config().capiPreviewPassword))
      .build

    httpClient.setConnectTimeout(5, TimeUnit.SECONDS)
    val resp = httpClient.newCall(req).execute

    resp.code match {
      case 200 => {
        Ok(resp.body.string).as(JSON)
      }
      case c => {
        BadRequest
      }
    }
  }
}
