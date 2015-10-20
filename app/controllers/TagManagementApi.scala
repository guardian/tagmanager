package controllers

import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import repositories.TagRepository

object TagManagementApi extends Controller with PanDomainAuthActions {

  def getTag(id: Long) = APIAuthAction {

    TagRepository.getTag(id).map{ tag =>
      Ok(Json.toJson(tag))
    }.getOrElse(NotFound)
  }

}
