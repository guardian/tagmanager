package controllers

import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import repositories.{TagLookupCache, TagSearchCriteria, TagRepository}

object TagManagementApi extends Controller with PanDomainAuthActions {

  def getTag(id: Long) = APIAuthAction {

    TagRepository.getTag(id).map{ tag =>
      Ok(Json.toJson(tag))
    }.getOrElse(NotFound)
  }

  def search = APIAuthAction { req =>

    val criteria = TagSearchCriteria(
      q = req.getQueryString("q"),
      types = req.getQueryString("types").map(_.split(",").toList)
    )

    val tags = TagLookupCache.search(criteria)

    Ok(Json.toJson(tags))
  }

}
