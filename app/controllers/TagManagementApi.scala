package controllers

import model.command.CommandError._
import model.command._
import model.jobs.Job
import org.joda.time.{DateTime, DateTimeZone}
import permissions.Permissions
import play.api.Logger
import model.jobs.JobRunner
import model._
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import repositories._
import permissions._

object TagManagementApi extends Controller with PanDomainAuthActions {

  def getTag(id: Long) = APIAuthAction {

    TagRepository.getTag(id).map{ tag =>
      Ok(Json.toJson(DenormalisedTag(tag)))
    }.getOrElse(NotFound)
  }

  def updateTag(id: Long) = (APIAuthAction andThen UpdateTagPermissionsCheck) { req =>
    implicit val username = Option(s"${req.user.firstName} ${req.user.lastName}")

    req.body.asJson.map { json =>
      try {
        UpdateTagCommand(json.as[DenormalisedTag]).process.map{t => Ok(Json.toJson(DenormalisedTag(t))) } getOrElse NotFound
      } catch {
        commandErrorAsResult
      }
    }.getOrElse {
      BadRequest("Expecting Json data")
    }
  }

  def createTag() = (APIAuthAction andThen CreateTagPermissionsCheck) { req =>
    implicit val username = Option(s"${req.user.firstName} ${req.user.lastName}")
    req.body.asJson.map { json =>
      try {
        json.as[CreateTagCommand].process.map{t => Ok(Json.toJson(DenormalisedTag(t))) } getOrElse NotFound
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
      types = req.getQueryString("types").map(_.split(",").toList),
      referenceType = req.getQueryString("referenceType")
    )

    val orderBy = req.getQueryString("orderBy").getOrElse("internalName")

    val tags = TagLookupCache.search(criteria)

    val orderedTags: List[Tag] = orderBy match {
      case("internalName") => tags.sortBy(_.internalName)
      case("externalName") => tags.sortBy(_.externalName)
      case("path") => tags.sortBy(_.path)
      case("id") => tags.sortBy(_.id)
      case("type") => tags.sortBy(_.`type`)
      case(_) => tags.sortBy(_.comparableValue)
    }

    val resultsCount = req.getQueryString("pageSize").getOrElse("25").toInt

    Ok(Json.toJson(orderedTags take resultsCount))
  }

  def getSection(id: Long) = APIAuthAction {
    SectionRepository.getSection(id).map{ section =>
      Ok(Json.toJson(section))
    }.getOrElse(NotFound)
  }

  def createSection() = (APIAuthAction andThen CreateSectionPermissionsCheck) { req =>
    implicit val username = Option(s"${req.user.firstName} ${req.user.lastName}")
    req.body.asJson.map { json =>
      try {
        json.as[CreateSectionCommand].process.map{t => Ok(Json.toJson(t)) } getOrElse NotFound
      } catch {
        commandErrorAsResult
      }
    }.getOrElse {
      BadRequest("Expecting Json data")
    }
  }

  def updateSection(id: Long) = (APIAuthAction andThen UpdateSectionPermissionsCheck) { req =>
    implicit val username = Option(s"${req.user.firstName} ${req.user.lastName}")
    req.body.asJson.map { json =>
      try {
        UpdateSectionCommand(json.as[Section]).process.map{ t => Ok(Json.toJson(t)) } getOrElse NotFound
      } catch {
        commandErrorAsResult
      }
    }.getOrElse {
      BadRequest("Expecting Json data")
    }
  }

  def addEditionToSection(id: Long) = (APIAuthAction andThen AddEditionToSectionPermissionsCheck) { req =>
    implicit val username = Option(s"${req.user.firstName} ${req.user.lastName}")
    req.body.asJson.map { json =>
      val editionName = (json \ "editionName").as[String]

      try {
        AddEditionToSectionCommand(id, editionName.toUpperCase).process.map{ t => Ok(Json.toJson(t)) } getOrElse NotFound
      } catch {
        commandErrorAsResult
      }
    }.getOrElse {
      BadRequest("Expecting Json data")
    }
  }

  def removeEditionFromSection(id: Long, editionName: String) = (APIAuthAction andThen RemoveEditionFromSectionPermissionsCheck) { req =>
    implicit val username = Option(s"${req.user.firstName} ${req.user.lastName}")
    try {
      RemoveEditionFromSectionCommand(id, editionName.toUpperCase).process.map{ t => Ok(Json.toJson(t)) } getOrElse NotFound
    } catch {
      commandErrorAsResult
    }
  }

  def listSections() = APIAuthAction {
    Ok(Json.toJson(SectionRepository.loadAllSections))
  }

  def listReferenceTypes() = APIAuthAction {
    Ok(Json.toJson(ExternalReferencesTypeRepository.loadAllReferenceTypes))
  }

  def checkPathInUse(`type`: String, slug: String, section: Option[Long], trackingTagType: Option[String]) = APIAuthAction { req =>
    try {
      new PathUsageCheck(`type`, slug, section, trackingTagType).process.map{ t => Ok(Json.toJson(t)) } getOrElse BadRequest
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

  def mergeTag = (APIAuthAction andThen MergeTagPermissionsCheck) { req =>
    implicit val username = Option(s"${req.user.firstName} ${req.user.lastName}")
    req.body.asJson.map { json =>
      try {
        json.as[MergeTagCommand].process.map{t => NoContent } getOrElse NotFound
      } catch {
        commandErrorAsResult
      }
    }.getOrElse {
      BadRequest("Expecting Json data")
    }
  }

  def deleteTag(id: Long) = (APIAuthAction andThen DeleteTagPermissionsCheck) { req =>
    implicit val username = Option(s"${req.user.firstName} ${req.user.lastName}")
    try {
      (new DeleteTagCommand(id)).process.map{t => NoContent } getOrElse NotFound
    } catch {
      commandErrorAsResult
    }
  }

  def searchSponsorships = APIAuthAction { req =>
    val criteria = SponsorshipSearchCriteria(
      q = req.getQueryString("q"),
      status = req.getQueryString("status"),
      `type` = req.getQueryString("type")
    )

    val orderBy = req.getQueryString("sortBy").getOrElse("internalName")

    val sponsorships = SponsorshipRepository.searchSponsorships(criteria)

    val orderedSponsorships: List[Sponsorship] = orderBy match {
      case("sponsor") => sponsorships.sortBy(_.sponsorName)
      case("from") => sponsorships.sortBy(_.validFrom.map(_.getMillis).getOrElse(0l))
      case("to") => sponsorships.sortBy(_.validTo.map(_.getMillis).getOrElse(Long.MaxValue))
      case("status") => sponsorships.sortBy(_.status)
      case(_) => sponsorships.sortBy(_.sponsorName)
    }

    val resultsCount = req.getQueryString("pageSize").getOrElse("25").toInt

    Ok(Json.toJson((orderedSponsorships take resultsCount).map(DenormalisedSponsorship(_))))
  }

  def getSponsorship(id: Long) = APIAuthAction { req =>
    Ok(Json.toJson(SponsorshipRepository.getSponsorship(id).map(DenormalisedSponsorship(_))))
  }

  def createSponsorship = (APIAuthAction andThen ManageSponsorshipsPermissionsCheck) { req =>
    implicit val username = Option(s"${req.user.firstName} ${req.user.lastName}")
    req.body.asJson.map { json =>
      try {
        json.as[CreateSponsorshipCommand].process.map{t => Ok(Json.toJson(t)) } getOrElse NotFound
      } catch {
        commandErrorAsResult
      }
    }.getOrElse {
      BadRequest("Expecting Json data")
    }
  }

  def updateSponsorship(id: Long) = (APIAuthAction andThen ManageSponsorshipsPermissionsCheck) { req =>
    implicit val username = Option(s"${req.user.firstName} ${req.user.lastName}")
    req.body.asJson.map { json =>
      try {
        json.as[UpdateSponsorshipCommand].process.map{s => Ok(Json.toJson(DenormalisedSponsorship(s))) } getOrElse NotFound
      } catch {
        commandErrorAsResult
      }
    }.getOrElse {
      BadRequest("Expecting Json data")
    }
  }

  def clashingSponsorships(id: Option[Long], tagIds: Option[String], sectionId: Option[Long], validFrom: Option[Long], validTo: Option[Long], editions: Option[String]) = APIAuthAction { req =>
    val editionSearch = editions.map(_.split(",").toList)
    val tagSearch: Option[List[Long]] = tagIds.map(_.split(",").toList.filter(_.length > 0).map(_.toLong))
    try {
      new ClashingSponsorshipsFetch(id, tagSearch, sectionId, validFrom.map(new DateTime(_)), validTo.map(new DateTime(_)), editionSearch)
        .process.map{ ss => Ok(Json.toJson(ss.map(DenormalisedSponsorship(_)))) } getOrElse BadRequest
    } catch {
      commandErrorAsResult
    }
  }

  def getAuditForTag(tagId: Long) = APIAuthAction { req =>
    Ok(Json.toJson(TagAuditRepository.getAuditTrailForTag(tagId)))
  }

  def getAuditForTagOperation(op: String) = APIAuthAction { req =>
    Ok(Json.toJson(TagAuditRepository.getRecentAuditOfTagOperation(op)))
  }

  def getAuditForSection(sectionId: Long) = APIAuthAction { req =>
    Ok(Json.toJson(SectionAuditRepository.getAuditTrailForSection(sectionId)))
  }

  def getAuditForSectionOperation(op: String) = APIAuthAction { req =>
    Ok(Json.toJson(SectionAuditRepository.getRecentAuditOfSectionOperation(op)))
  }

  def getJobs(tagIdParam: Option[Long]) = APIAuthAction {
    val jobs = tagIdParam match {
      case Some(tagId) => JobRepository.findJobsForTag(tagId)
      case None => JobRepository.loadAllJobs
    }
    Ok(Json.toJson(jobs))
  }

  def deleteJob(jobId: Long) = (APIAuthAction andThen JobDeletePermissionsCheck) { req =>
    try {
      JobRepository.deleteIfTerminal(jobId)
      Ok
    } catch {
      case e: Exception => BadRequest("Job was not in a completed or failed state")
    }
  }

  def rollbackJob(id: Long) = (APIAuthAction andThen JobRollbackPermissionsCheck) { req =>
    val currentTime = new DateTime(DateTimeZone.UTC).getMillis
    val lockBreakTime = currentTime - JobRunner.lockTimeOutMillis
    val nodeId = JobRunner.nodeId

    JobRepository.getJob(id)
      .flatMap(JobRepository.lock(_, nodeId, currentTime, lockBreakTime))
      .map(job => {
        try {
          job.rollback
          Ok
        } catch {
          case e: Exception => InternalServerError("Failed to rollback")
        } finally {
          JobRepository.upsertJobIfOwned(job, nodeId)
          JobRepository.unlock(job, nodeId)
        }
      }).getOrElse(NotFound)
  }
}
