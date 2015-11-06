package controllers

import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import repositories._

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

  def searchTags = APIAuthAction { req =>

    val criteria = TagSearchCriteria(
      q = req.getQueryString("q"),
      types = req.getQueryString("types").map(_.split(",").toList)
    )

    val tags = TagLookupCache.search(criteria) take 25

    Ok(Json.toJson(tags))
  }

  def getSection(id: Long) = APIAuthAction {

    SectionRepository.getSection(id).map{ section =>
      Ok(Json.toJson(section))
    }.getOrElse(NotFound)
  }

  def updateSection(id: Long) = APIAuthAction { req =>

    req.body.asJson.map { json =>
      SectionRepository.updateSection(json).map{ section =>
        Ok(Json.toJson(section))
      }.getOrElse(BadRequest("Could not update section"))

    }.getOrElse {
      BadRequest("Expecting Json data")
    }
  }

  def listSections() = APIAuthAction {
    Ok(Json.toJson(SectionRepository.loadAllSections))
  }

  def checkPathInUse(path: String) = APIAuthAction { Ok(Json.toJson(Map("inUse" -> PathManager.isPathInUse(path)))) }
}
