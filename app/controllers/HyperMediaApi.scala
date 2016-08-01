package controllers

import model.{Tag, EntityResponse, EmptyResponse, CollectionResponse, EmbeddedEntity, TagEntity, HyperMediaHelpers}
import play.api.libs.json._
import play.api.mvc.Result
import play.api.mvc.{Action, Controller, Request, Result}
import repositories._
import scala.concurrent.Future
import services.Config.conf
import scala.concurrent.ExecutionContext.Implicits.global


case class CORSable[A](origins: String*)(action: Action[A]) extends Action[A] {
  def apply(request: Request[A]): Future[Result] = {
    val headers = request.headers.get("Origin").map { origin =>
      if(origins.contains(origin)) {
        List(CORSable.CORS_ALLOW_ORIGIN -> origin, CORSable.CORS_CREDENTIALS -> "true")
      } else { Nil }
    }

    action(request).map(_.withHeaders(headers.getOrElse(Nil) :_*))
  }

  lazy val parser = action.parser
}

object CORSable {
  val CORS_ALLOW_ORIGIN = "Access-Control-Allow-Origin"
  val CORS_CREDENTIALS = "Access-Control-Allow-Credentials"
  val CORS_ALLOW_METHODS = "Access-Control-Allow-Methods"
  val CORS_ALLOW_HEADERS = "Access-Control-Allow-Headers"
}

object HyperMediaApi extends Controller with PanDomainAuthActions {
  def hyper = CORSable(conf.corsableDomains: _*) {
    Action {
      val res = EmptyResponse()
        .addLink("tag-item", HyperMediaHelpers.fullUri("/hyper/tags/{id}"))
        .addLink("tags", HyperMediaHelpers.fullUri("/hyper/tags{?offset,limit,query,type,internalName,externalName,externalReferenceType,externalReferenceToken,subType}"))
      Ok(Json.toJson(res))
    }
  }

  def tag(id: Long) = CORSable(conf.corsableDomains: _*) {
    Action {
      TagRepository.getTag(id).map { tag =>
        Ok(Json.toJson(EntityResponse(TagEntity(tag))))
      }.getOrElse(NotFound)
    }
  }

  def tags = CORSable(conf.corsableDomains: _*) {
    Action { implicit req =>
      // we need to map keyword to topic in the types changes as flex searches for keywords - this might change (07/01/2016)
      val types = req.getQueryString("type").map(_.replaceAll(" ", "").split(",").toList.map { x =>
        if(x.toLowerCase() == "keyword") "topic" else x
      })

      val criteria = TagSearchCriteria(
        q = req.getQueryString("query"),
        searchField = req.getQueryString("searchField"),
        types = types,
        referenceType = req.getQueryString("externalReferenceType"),
        internalName = req.getQueryString("internalName"),
        externalName = req.getQueryString("externalName"),
        referenceToken = req.getQueryString("externalReferenceToken"),
        subType = req.getQueryString("subType")
      )

      val limit = req.getQueryString("limit").getOrElse("25").toInt
      val offset = req.getQueryString("offset").getOrElse("0").toInt

      val tags = TagLookupCache.search(criteria).drop(offset).take(limit).map { tag =>
        EmbeddedEntity(HyperMediaHelpers.tagUri(tag.id), Some(TagEntity(tag)))
      }

      Ok(Json.toJson(CollectionResponse(offset, limit, Some(tags.size), tags)))
    }
  }

  def preflight(routes: String) = CORSable(conf.corsableDomains: _*) {
    Action { implicit req =>
      val requestedHeaders = req.headers.get("Access-Control-Request-Headers")

      NoContent.withHeaders(
        CORSable.CORS_ALLOW_METHODS -> "GET, DELETE, PUT",
        CORSable.CORS_ALLOW_HEADERS -> requestedHeaders.getOrElse(""))
    }
  }
}
