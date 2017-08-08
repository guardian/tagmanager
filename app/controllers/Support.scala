package controllers

import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit

import com.amazonaws.services.s3.model._
import com.squareup.okhttp.{Credentials, OkHttpClient, Request}
import model.command.CommandError._
import model.command.{ExpireSectionContentCommand, UnexpireSectionContentCommand, UpdateTagCommand}
import model.{DenormalisedTag, Image, ImageAsset}
import org.joda.time.DateTime
import permissions.ModifySectionExpiryPermissionsCheck
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.{JsString, Json}
import play.api.mvc.{Action, Controller}
import repositories.{TagLookupCache, TagRepository}
import services.{AWS, Config, ImageMetadataService}

import scala.concurrent.Future
import scala.util.control.NonFatal


object Support extends Controller with PanDomainAuthActions {

  def imageMetadata(imageUrl: String = "") = APIAuthAction {
    ImageMetadataService.fetch(imageUrl).map { imageMetadata =>
      Ok(Json.toJson(imageMetadata))
    }.getOrElse(NotFound)
  }

  def validateImageDimensions(image: File, requiredWidth: Option[Long], requiredHeight: Option[Long]): Boolean = {
    (requiredWidth, requiredHeight) match {
      case (None, None) => true
      case (Some(width), None) => width >= ImageMetadataService.imageWidth(image)
      case (None, Some(height)) => height >= ImageMetadataService.imageHeight(image)
      case (Some(width), Some(height)) => width >= ImageMetadataService.imageWidth(image) && height >= ImageMetadataService.imageHeight(image)
    }
  }

  def uploadLogo(filename: String) = APIAuthAction(parse.temporaryFile) { req =>
    val picture = req.body
    val requiredWidth = req.getQueryString("width").map(_.toLong)
    val requiredHeight = req.getQueryString("height").map(_.toLong)

    validateImageDimensions(picture.file, requiredWidth, requiredHeight) match {
      case true => {
        val dateSlug = new DateTime().toString("dd/MMM/yyyy")
        val logoPath = s"commercial/sponsor/${dateSlug}/${UUID.randomUUID}-${filename}"
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
        }.getOrElse(BadRequest("Failed to upload image"))
      }
      case false => BadRequest(s"Image must have dimensions w:${requiredWidth.getOrElse("Not Specified")} h:${requiredHeight.getOrElse("Not Specified")}")
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

  def flexPathMigrationSpecificData = Action {
    Ok(
      Json.toJson(TagLookupCache.allTags.get.map(tag => tag.id.toString -> JsString(tag.path)).toMap)
    )
  }

  def flexSlugMigrationSpecificData = Action {
    Ok(
      Json.toJson(TagLookupCache.allTags.get.map(tag => tag.id.toString -> JsString(tag.slug)).toMap)
    )
  }

  def unexpireSectionContent = (APIAuthAction andThen ModifySectionExpiryPermissionsCheck).async { req =>
    implicit val username = Option(req.user.email)
    req.body.asJson.map { json =>
      val sectionId = (json \ "sectionId").as[Long]

      UnexpireSectionContentCommand(sectionId).process.map{ result =>
        result.map(t => Ok("Unexpiry Completed Successfully")) getOrElse BadRequest("Failed to trigger unexpiry")

      } recover {
        commandErrorAsResult
      }
    }.getOrElse {
      Future.successful(BadRequest("Expecting sectionId in JSON"))
    }
  }

  def expireSectionContent = (APIAuthAction andThen ModifySectionExpiryPermissionsCheck).async { req =>

    implicit val username = Option(req.user.email)
    req.body.asJson.map { json =>
      val sectionId = (json \ "sectionId").as[Long]

      ExpireSectionContentCommand(sectionId).process.map { result =>
        result.map(t => Ok("Expiry Completed Successfully")) getOrElse BadRequest("Failed to trigger expiry")
      } recover {
        commandErrorAsResult
      }
    }.getOrElse {
      Future.successful(BadRequest("Expecting sectionId in JSON"))
    }

  }

  // TODO delete this!
  def unexpireTag = APIAuthAction { req =>
    implicit val username = Option(req.user.email)

    req.body.asJson.map { json =>
      val tagId = (json \ "tagId").as[Long]
      import repositories.SponsorshipOperations
      try {
        SponsorshipOperations.unexpirePaidContentTag(tagId)

        Ok
      } catch {
        case NonFatal(e) => BadRequest(e.toString)
      }
    }.getOrElse {
        BadRequest("Expecting tagId in JSON")
    }
  }

  def fixDanglingParents = APIAuthAction.async { req =>

    implicit val username = Option(req.user.email)

    val knownTags = TagRepository.loadAllTags
    var danglingParentsCount = 0

    val futures = knownTags flatMap {tag =>
      tag.parents.flatMap { parentId =>
        if (!knownTags.exists(tag => tag.id == parentId)) {

          Logger.info(s"Tag ID: ${tag.id}, detected dangling parent $parentId")

          val updatedTag = tag.copy(parents = tag.parents.filterNot(_.equals(parentId)))

          Some(UpdateTagCommand(DenormalisedTag(updatedTag)).process.map {result =>
            result getOrElse InternalServerError(s"Could not update tag: ${tag.id}")
            danglingParentsCount += 1
          })
        } else None
      }
    }
    Future.sequence(futures).map { _ =>
      Ok(s"Removed $danglingParentsCount Dangling Parents")
    }
  }
}
