package controllers

import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import play.api.Logger
import services.ImageMetadataService

object Support extends Controller with PanDomainAuthActions {

  def imageMetadata(imageUrl: String = "") = AuthAction {

    val imageMetadata = ImageMetadataService.fetch(imageUrl)
    Ok(Json.toJson(imageMetadata))

    ImageMetadataService.fetch(imageUrl).map { imageMetadata =>
      Ok(Json.toJson(imageMetadata))
    }.getOrElse(NotFound)

  }


}
