package controllers

import model.command.CommandError._
import model.command.{BatchTagCommand, PathUsageCheck, CreateTagCommand, UpdateTagCommand}
import model.jobs.{BatchTagAddCompleteCheck, Job}
import org.joda.time.DateTime
import play.api.Logger
import model.Tag
import model.Section
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import repositories._

object TagManagementApi extends Controller with PanDomainAuthActions {

  def getTag(id: Long) = APIAuthAction {

    TagRepository.getTag(id).map{ tag =>
      Ok(Json.toJson(tag))
    }.getOrElse(NotFound)
  }

  def updateTag(id: Long) = APIAuthAction { req =>
    implicit val username = Option(s"${req.user.firstName} ${req.user.lastName}")

    req.body.asJson.map { json =>
      try {
        UpdateTagCommand(json.as[Tag]).process.map{t => Ok(Json.toJson(t)) } getOrElse NotFound
      } catch {
        commandErrorAsResult
      }
    }.getOrElse {
      BadRequest("Expecting Json data")
    }
  }

  def createTag() = APIAuthAction { req =>
    implicit val username = Option(s"${req.user.firstName} ${req.user.lastName}")
    req.body.asJson.map { json =>
      try {
        json.as[CreateTagCommand].process.map{t => Ok(Json.toJson(t)) } getOrElse NotFound
      } catch {
        commandErrorAsResult
      }
    }.getOrElse {
      BadRequest("Expecting Json data")
    }
  }

  def searchTags = APIAuthAction { req =>

    val criteria = TagSearchCriteria(
      q = req.getQueryString("q"),
      searchField = req.getQueryString("searchField"),
      types = req.getQueryString("types").map(_.split(",").toList)
    )

    val orderBy = req.getQueryString("orderBy").getOrElse("internalName");

    val tags = TagLookupCache.search(criteria)

    val orderedTags: List[Tag] = orderBy match {
      case("internalName") => tags.sortBy(_.internalName)
      case("externalName") => tags.sortBy(_.externalName)
      case("path") => tags.sortBy(_.path)
      case("id") => tags.sortBy(_.id)
      case("type") => tags.sortBy(_.`type`)
      case(_) => tags.sortBy(_.comparableValue)
    }

    Ok(Json.toJson(orderedTags take 25))
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

  def listReferenceTypes() = APIAuthAction {
    Ok(Json.toJson(ExternalReferencesTypeRepository.loadAllReferenceTypes))
  }

  def checkPathInUse(`type`: String, slug: String, section: Option[Long]) = APIAuthAction { req =>
    try {
      new PathUsageCheck(`type`, slug, section).process.map{ t => Ok(Json.toJson(t)) } getOrElse BadRequest
    } catch {
      commandErrorAsResult
    }
  }

  def batchTag = APIAuthAction { req =>
    implicit val username = Option(s"${req.user.firstName} ${req.user.lastName}")
    req.body.asJson.map { json =>
      try {
        json.as[BatchTagCommand].process.map{t => NoContent } getOrElse NotFound
      } catch {
        commandErrorAsResult
      }
    }.getOrElse {
      BadRequest("Expecting Json data")
    }
  }

  def getAuditForTag(tagId: Long) = APIAuthAction { req =>
    Ok(Json.toJson(TagAuditRepository.getAuditTrailForTag(tagId)))
  }

  def getAuditForOperation(op: String) = APIAuthAction { req =>
    Ok(Json.toJson(TagAuditRepository.getRecentAuditOfOperation(op)))
  }
  
  def getJobs(tagIdParam: Option[Long]) = APIAuthAction { 
    val jobs = tagIdParam match {
      case Some(tagId) => JobRepository.findJobsForTag(tagId)
      case None => JobRepository.loadAllJobs
    }
    Ok(Json.toJson(jobs))
  }

}
