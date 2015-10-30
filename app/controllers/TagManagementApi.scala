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

  def updateTag(id: Long) = APIAuthAction { req =>

    req.body.asJson.map { json =>
      TagRepository.updateTag(json).map{ tag =>
        Ok(Json.toJson(tag))
      }.getOrElse(BadRequest("Could not update tag"))

    }.getOrElse {
      BadRequest("Expecting Json data")
    }
  }

  def search = APIAuthAction { req =>

    val criteria = TagSearchCriteria(
      q = req.getQueryString("q"),
      types = req.getQueryString("types").map(_.split(",").toList)
    )

    val tags = TagLookupCache.search(criteria) take 25

    Ok(Json.toJson(tags))
  }

}
