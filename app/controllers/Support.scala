package controllers

import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.ImageMetadataService

object Support extends Controller with PanDomainAuthActions {

  def imageMetadata(imageUrl: String = "") = APIAuthAction {
    ImageMetadataService.fetch(imageUrl).map { imageMetadata =>
      Ok(Json.toJson(imageMetadata))
    }.getOrElse(NotFound)
  }
}
