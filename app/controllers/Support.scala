package controllers

import com.amazonaws.services.s3.model._
import model.{DenormalisedTag, Image, ImageAsset}
import org.joda.time.DateTime
import com.squareup.okhttp.{Credentials, OkHttpClient, Request}
import model.command.{UnexpireSectionContentCommand, ExpireSectionContentCommand, UpdateTagCommand}
import model.command.CommandError._
import play.api.Logger
import play.api.libs.json.{JsString, Json}
import play.api.mvc.{Action, Controller}
import repositories.{TagLookupCache, TagRepository}
import services.{AWS, Config, ImageMetadataService}
import com.squareup.okhttp.{Credentials, OkHttpClient, Request}
import repositories.TagLookupCache
import services.{Config, ImageMetadataService}
import java.util.concurrent.TimeUnit

import permissions.ModifySectionExpiryPermissionsCheck


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

        AWS.frontendStaticFilesS3Client.putObject(
          new PutObjectRequest("static-theguardian-com", logoPath, picture.file)
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

  def flexMigrationSpecificData = Action {
    Ok(
      Json.toJson(TagLookupCache.allTags.get.map(tag => tag.id.toString -> JsString(tag.path)).toMap)
    )
  }

  def unexpireSectionContent = (APIAuthAction andThen ModifySectionExpiryPermissionsCheck) { req =>

    implicit val username = Option(s"${req.user.firstName} ${req.user.lastName}")
    req.body.asJson.map { json =>
      val sectionId = (json \ "sectionId").as[Long]

      try {
        UnexpireSectionContentCommand(sectionId).process.map(t => Ok("Unexpiry Completed Successfully")) getOrElse BadRequest("Failed to trigger unexpiry")

      } catch {
        commandErrorAsResult
      }
    }.getOrElse {
      BadRequest("Expecting sectionId in JSON")
    }
  }

  def expireSectionContent = (APIAuthAction andThen ModifySectionExpiryPermissionsCheck) { req =>

    implicit val username = Option(s"${req.user.firstName} ${req.user.lastName}")
    req.body.asJson.map { json =>
      val sectionId = (json \ "sectionId").as[Long]

      try {
        ExpireSectionContentCommand(sectionId).process.map(t => Ok("Expiry Completed Successfully")) getOrElse BadRequest("Failed to trigger expiry")

      } catch {
        commandErrorAsResult
      }
    }.getOrElse {
      BadRequest("Expecting sectionId in JSON")
    }

  }

  def fixDanglingParents = APIAuthAction { req =>

    implicit val username = Option(s"${req.user.firstName} ${req.user.lastName}")

    val knownTags = TagRepository.loadAllTags
    var danglingParentsCount = 0

    knownTags foreach {tag =>
      tag.parents.foreach { parentId =>
        if (knownTags.filter(tag => tag.id == parentId).isEmpty) {

          Logger.info(s"Tag ID: ${tag.id}, detected dangling parent ${parentId}")

          val updatedTag = tag.copy(parents = tag.parents.filterNot(_.equals(parentId)))

          UpdateTagCommand(DenormalisedTag(updatedTag)).process getOrElse InternalServerError(s"Could not update tag: ${tag.id}")
          danglingParentsCount += 1
        }
      }
    }

    Ok(s"Removed $danglingParentsCount Dangling Parents")
  }
}
