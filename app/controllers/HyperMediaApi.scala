package controllers

import model.{Tag, EntityResponse, EmptyResponse, CollectionResponse, EmbeddedEntity}
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import repositories._
import services.Config.conf


object HyperMediaApi extends Controller with PanDomainAuthActions {
  def tag(id: Long) = APIAuthAction {
    TagRepository.getTag(id).map { tag =>
      Ok(Json.toJson(EntityResponse(tag)))
    }.getOrElse(NotFound)
  }

  def tags = APIAuthAction { req =>
    if(req.queryString.isEmpty) {
      val res = EmptyResponse()
        .addLink("tags", "/api/tag/{id}")
        .addLink("tags", "/tags{?offset,limit,query,type,internalName,externalName,externalReferenceType,externalReferenceToken}")
      Ok(Json.toJson(res))

    } else {
      val criteria = TagSearchCriteria(
        q = req.getQueryString("query"),
        searchField = req.getQueryString("searchField"),
        types = req.getQueryString("types").map(_.split(",").toList),
        referenceType = req.getQueryString("externalReferenceType"),
        internalName = req.getQueryString("internalName"),
        externalName = req.getQueryString("externalName"),
        referenceToken = req.getQueryString("externalReferenceToken")
      )

      val limit = req.getQueryString("limit").getOrElse("25").toInt
      val offset = req.getQueryString("offset").getOrElse("0").toInt

      val tags = TagLookupCache.search(criteria).drop(offset).take(limit).map { tag =>
        EmbeddedEntity(tagUri(tag.id), Some(tag))
      }

      val res = CollectionResponse(0, limit, Some(tags.size), tags)
      Ok(Json.toJson(res))
}
  }

  def tagUri(id: Long): String = s"https://tagmanager.${conf.pandaDomain}/hyper/tags/${id}"
}
