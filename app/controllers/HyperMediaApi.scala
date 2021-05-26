package controllers

import com.gu.pandomainauth.PanDomainAuthSettingsRefresher
import model._
import play.api.libs.json._
import play.api.mvc.{BaseController, ControllerComponents}
import repositories._
import services.Config.conf
import helpers.CORSable
import play.api.Logging
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext

class HyperMediaApi(
  val wsClient: WSClient,
  override val controllerComponents: ControllerComponents,
  val panDomainSettings: PanDomainAuthSettingsRefresher
)(
  implicit ec: ExecutionContext
)
  extends BaseController
  with PanDomainAuthActions
  with Logging {

  def hyper = CORSable(conf.corsableDomains: _*) {
    Action {
      val res = EmptyResponse()
        .addLink("tag-sponsorships", HyperMediaHelpers.fullUri("/hyper/tags/{id}/sponsorships"))
        .addLink("sponsorship-item", HyperMediaHelpers.fullUri("/hyper/sponsorships/{id}"))
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

  def sponsorship(id: Long) = CORSable(conf.corsableDomains: _*) {
    Action {
      SponsorshipRepository.getSponsorship(id).map { s =>
        Ok(Json.toJson(EntityResponse(s)))
      }.getOrElse(NotFound)
    }
  }

  def tagSponsorships(id: Long) = CORSable(conf.corsableDomains: _*) {
    Action {
      TagRepository.getTag(id).map { tag =>
        val activeSponsorships = tag.activeSponsorships.flatMap{sid => SponsorshipRepository.getSponsorship(sid)}
        val paidSponsorship = tag.sponsorship.flatMap(SponsorshipRepository.getSponsorship(_))

        val sponsorships = (activeSponsorships ::: paidSponsorship.toList).distinct

        Ok(Json.toJson(CollectionResponse(
          offset = 0,
          limit = sponsorships.length,
          total = Some(sponsorships.length),
          data = sponsorships.map{ s => EmbeddedEntity(HyperMediaHelpers.sponsorshipUri(s.id), Some(s))}
        )))
      }.getOrElse(NotFound)
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
