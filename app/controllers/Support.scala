package controllers

import com.amazonaws.services.s3.model._
import model.{ImageAsset, Image}
import org.joda.time.DateTime
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.{AWS, ImageMetadataService}

object Support extends Controller with PanDomainAuthActions {

  def imageMetadata(imageUrl: String = "") = APIAuthAction {
    ImageMetadataService.fetch(imageUrl).map { imageMetadata =>
      Ok(Json.toJson(imageMetadata))
    }.getOrElse(NotFound)
  }

  def uploadLogo(filename: String) = APIAuthAction(parse.temporaryFile) { req =>
    val picture = req.body

    ImageMetadataService.imageDimensions(picture.file) match {
      case (140, 90) => {
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
        }.getOrElse(NotFound)
      }
      case _ => BadRequest("sponsorship logos must be 140 x 90")
    }
  }
}
