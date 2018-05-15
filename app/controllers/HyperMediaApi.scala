package controllers

import model._
import play.api.libs.json._
import play.api.mvc.{Action, Controller, Request, Result}
import repositories._
import services.Config.conf

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

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
  def hyper = PerfAction {
    CORSable(conf.corsableDomains: _*) {
      Action {
        val res = EmptyResponse()
          .addLink("tag-sponsorships", HyperMediaHelpers.fullUri("/hyper/tags/{id}/sponsorships"))
          .addLink("sponsorship-item", HyperMediaHelpers.fullUri("/hyper/sponsorships/{id}"))
          .addLink("tag-item", HyperMediaHelpers.fullUri("/hyper/tags/{id}"))
          .addLink("tags", HyperMediaHelpers.fullUri("/hyper/tags{?offset,limit,query,type,internalName,externalName,externalReferenceType,externalReferenceToken,subType}"))
        Ok(Json.toJson(res))
      }
    }
  }

  def tag(id: Long) = PerfAction {
    CORSable(conf.corsableDomains: _*) {
      Action {
        TagRepository.getTag(id).map { tag =>
          Ok(Json.toJson(EntityResponse(TagEntity(tag))))
        }.getOrElse(NotFound)
      }
    }
  }

  def tags = PerfAction {
    CORSable(conf.corsableDomains: _*) {
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
  }

  def sponsorship(id: Long) = PerfAction {
    CORSable(conf.corsableDomains: _*) {
      Action {
        SponsorshipRepository.getSponsorship(id).map { s =>
          Ok(Json.toJson(EntityResponse(s)))
        }.getOrElse(NotFound)
      }
    }
  }

  def tagSponsorships(id: Long) = PerfAction {
    CORSable(conf.corsableDomains: _*) {
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
