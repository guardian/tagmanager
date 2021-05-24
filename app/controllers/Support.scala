package controllers

import java.io.File
import java.net.URI
import java.util.UUID
import java.util.concurrent.TimeUnit
import com.amazonaws.services.s3.model._
import com.gu.pandomainauth.PanDomainAuthSettingsRefresher
import model.command.CommandError._
import model.command.{ExpireSectionContentCommand, UnexpireSectionContentCommand, UpdateTagCommand}
import model.{DenormalisedTag, Image, ImageAsset}
import okhttp3.{Headers, OkHttpClient, Request}
import org.joda.time.DateTime
import helpers.JodaDateTimeFormat._
import permissions.ModifySectionExpiryPermissionsCheck
import play.api.Logging
import play.api.libs.json.{JsString, Json}
import play.api.libs.ws.WSClient
import play.api.mvc.{BaseController, ControllerComponents}
import repositories.{ContentAPI, TagLookupCache, TagRepository}
import services.{AWS, Config, FetchError, ImageMetadataService, InvalidImage}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import collection.JavaConverters._

class Support(
  val wsClient: WSClient,
  override val controllerComponents: ControllerComponents,
  val panDomainSettings: PanDomainAuthSettingsRefresher
)(
  implicit ec: ExecutionContext
)
  extends BaseController
  with PanDomainAuthActions
  with Logging {

  private val httpClient = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS).build

  def imageMetadata(imageUrl: String = "") = APIAuthAction {
     ImageMetadataService.fetch(imageUrl) match {
       case Right(imageMetadata) =>  Ok(Json.toJson(imageMetadata))
       case Left(err: FetchError) => NotFound(err.errMsg)
       case Left(err: InvalidImage) => UnprocessableEntity(err.errMsg)
     }
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

        ImageMetadataService.fetch(uploadedImageUrl) match {
          case Right(imageMetadata) =>
            val image = Image(imageMetadata.mediaId, List(
            ImageAsset(
              imageUrl = uploadedImageUrl,
              width = imageMetadata.width,
              height = imageMetadata.height,
              mimeType = imageMetadata.mimeType
            )
          ))
            Ok(Json.toJson(image))
          case Left(err: FetchError) => InternalServerError(err.errMsg)
          case Left(err: InvalidImage) => UnprocessableEntity(err.errMsg)
        }
      }
      case false => BadRequest(s"Image must have dimensions w:${requiredWidth.getOrElse("Not Specified")} h:${requiredHeight.getOrElse("Not Specified")}")
    }
  }

  def previewCapiProxy(path: String) = APIAuthAction { request =>
    val url = s"${Config().capiPreviewIAMUrl}/$path?${request.rawQueryString}"
    logger.info(s"Requesting CAPI preview -> $url")

    val req = new Request.Builder()
      .url(url)
      .headers(Headers.of(ContentAPI.signer.addIAMHeaders(Map.empty, URI.create(url)).asJava))
      .build

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

  def unexpireSectionContent = (APIAuthAction andThen ModifySectionExpiryPermissionsCheck()).async { req =>
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

  def expireSectionContent = (APIAuthAction andThen ModifySectionExpiryPermissionsCheck()).async { req =>

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

          logger.info(s"Tag ID: ${tag.id}, detected dangling parent $parentId")

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
