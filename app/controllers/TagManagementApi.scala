package controllers

import com.gu.pandomainauth.PanDomainAuthSettingsRefresher
import model._
import model.command.CommandError._
import model.command._
import model.jobs.JobRunner
import helpers.JodaDateTimeFormat._
import org.joda.time.{DateTime, DateTimeZone}
import permissions._
import play.api.libs.json._
import play.api.mvc.{BaseController, ControllerComponents, Result}
import repositories._
import services.Config
import helpers.CORSable
import model.forms.{FilterTypes, GetSpreadSheet}
import play.api.Logging
import play.api.libs.ws.WSClient
import services.Config.conf

import scala.concurrent.{ExecutionContext, Future}

class TagManagementApi(
  val wsClient: WSClient,
  override val controllerComponents: ControllerComponents,
  val panDomainSettings: PanDomainAuthSettingsRefresher
)(
  implicit ec: ExecutionContext
)
  extends BaseController
    with PanDomainAuthActions
    with Logging {

  def getTag(id: Long) = APIAuthAction {

    TagRepository.getTag(id).map{ tag =>
      Ok(Json.toJson(DenormalisedTag(tag)))
    }.getOrElse(NotFound)
  }

  def updateTag(id: Long) =
    (APIAuthAction andThen UpdateTagPermissionsCheck() andThen UpdateTagSpecificPermissionsCheck()).async { req =>
    implicit val username = Option(req.user.email)

    req.body.asJson.map { json =>
      UpdateTagCommand(json.as[DenormalisedTag]).process().map { result =>
        result.map{t => Ok(Json.toJson(DenormalisedTag(t))) } getOrElse NotFound
      } recover {
        commandErrorAsResult
      }
    }.getOrElse {
      Future.successful(BadRequest("Expecting Json data"))
    }
  }

  def createTag = CORSable(conf.corsablePostDomains: _*) {
    (APIAuthAction andThen CreateTagPermissionsCheck() andThen CreateTagSpecificPermissionsCheck()).async { req =>
      implicit val username = Option(req.user.email)
      req.body.asJson.map { json =>
        json.as[CreateTagCommand].process().map { result =>
          result.map { t => Ok(Json.toJson(DenormalisedTag(t))) } getOrElse NotFound
        } recover {
          commandErrorAsResult
        }
      }.getOrElse {
        Future.successful(BadRequest("Expecting Json data"))
      }
    }
  }

  def searchTags = APIHMACAuthAction { req =>
    val criteria = TagSearchCriteria(
      q = req.getQueryString("q"),
      searchField = req.getQueryString("searchField"),
      types = req.getQueryString("types").map(_.split(",").toList.map(_.trim())),
      referenceType = req.getQueryString("referenceType"),
      hasFields = req.getQueryString("hasFields").map(_.split(",").toList.map(_.trim()))
    )

    val orderBy = req.getQueryString("orderBy").getOrElse("internalName")

    val tags = TagLookupCache.search(criteria)

    val orderedTags: List[Tag] = orderBy match {
      case ("internalName") => tags.sortBy(_.internalName)
      case ("externalName") => tags.sortBy(_.externalName)
      case ("path") => tags.sortBy(_.path)
      case ("id") => tags.sortBy(_.id)
      case ("type") => tags.sortBy(_.`type`)
      case (_) => tags.sortBy(_.comparableValue)
    }

    val page = req.getQueryString("page").getOrElse("1").toInt
    val pageSize = req.getQueryString("pageSize").map(_.toInt).getOrElse(Config().tagSearchPageSize)

    val startIndex = (page - 1) * pageSize
    val paginatedTagResults = orderedTags.drop(startIndex).take(pageSize)
    val tagCount = orderedTags.length

    Ok(Json.toJson(TagSearchResult(paginatedTagResults, tagCount)))
  }

  def spreadsheet = APIHMACAuthAction(parse.json[GetSpreadSheet]) { req =>
    val tags = req.body.filters.map { f =>
      f.`type` match {
        case FilterTypes.HasFields =>
          TagSearchCriteria(
            hasFields = Some(f.value.split(',').toList)
          )
        case _ =>
          TagSearchCriteria(
            q = Some(f.value),
            searchField = Some(f.`type`.entryName)
          )
      }
    }.foldLeft(TagLookupCache.allTags.get) { (acc, criteria) =>
      criteria.execute(acc)
    }

    Ok(Json.toJson(TagSearchResult(tags, tags.length)))
  }

  def getSection(id: Long) = APIAuthAction {
    SectionRepository.getSection(id).map{ section =>
      Ok(Json.toJson(section))
    }.getOrElse(NotFound)
  }

  def createSection() = (APIAuthAction andThen CreateSectionPermissionsCheck()).async { req =>
    implicit val username = Option(req.user.email)
    req.body.asJson.map { json =>
      json.as[CreateSectionCommand].process().map { result =>
        result.map{t => Ok(Json.toJson(t)) } getOrElse NotFound
      } recover {
        commandErrorAsResult
      }
    }.getOrElse {
      Future.successful(BadRequest("Expecting Json data"))
    }
  }

  def updateSection(id: Long) = (APIAuthAction andThen UpdateSectionPermissionsCheck()).async { req =>
    implicit val username = Option(req.user.email)
    req.body.asJson.map { json =>
      UpdateSectionCommand(json.as[Section]).process().map { result =>
        result.map{ t => Ok(Json.toJson(t)) } getOrElse NotFound
      } recover {
        commandErrorAsResult
      }
    }.getOrElse {
      Future.successful(BadRequest("Expecting Json data"))
    }
  }

  def addEditionToSection(id: Long) = (APIAuthAction andThen AddEditionToSectionPermissionsCheck()).async { req =>
    implicit val username = Option(req.user.email)
    req.body.asJson.map { json =>
      val editionName = (json \ "editionName").as[String]

      AddEditionToSectionCommand(id, editionName.toUpperCase).process().map { result =>
        result.map{ t => Ok(Json.toJson(t)) } getOrElse NotFound
      } recover {
        commandErrorAsResult
      }
    }.getOrElse {
      Future.successful(BadRequest("Expecting Json data"))
    }
  }

  def removeEditionFromSection(id: Long, editionName: String) = (APIAuthAction andThen RemoveEditionFromSectionPermissionsCheck()).async { req =>
    implicit val username = Option(req.user.email)
    RemoveEditionFromSectionCommand(id, editionName.toUpperCase).process().map {result =>
      result.map{ t => Ok(Json.toJson(t)) } getOrElse NotFound
    } recover {
      commandErrorAsResult
    }
  }

  def listSections() = APIAuthAction {
    Ok(Json.toJson(SectionRepository.loadAllSections))
  }

  def listPillars() = APIAuthAction {
    Ok(Json.toJson(PillarRepository.loadAllPillars))
  }

  def getPillar(id: Long) = APIAuthAction {
    PillarRepository.getPillar(id).map { pillar =>
      Ok(Json.toJson(pillar))
    }.getOrElse(NotFound)
  }

  def createPillar() = (APIAuthAction andThen PillarPermissionsCheck()).async { req =>
    implicit val username = Option(req.user.email)
    req.body.asJson.map { json =>
      json.as[CreatePillarCommand].process().map { result =>
        result.map{t => Ok(Json.toJson(t)) } getOrElse NotFound
      } recover {
        commandErrorAsResult
      }
    }.getOrElse {
      Future.successful(BadRequest("Expecting Json data"))
    }
  }

  def updatePillar(id: Long) = (APIAuthAction andThen PillarPermissionsCheck()).async { req =>
    implicit val username = Option(req.user.email)
    req.body.asJson.map { json =>
      UpdatePillarCommand(json.as[Pillar]).process().map { result =>
        result.map { t => Ok(Json.toJson(t)) } getOrElse NotFound
      } recover {
        commandErrorAsResult
      }
    }.getOrElse {
      Future.successful(BadRequest("Expecting Json data"))
    }
  }

  def deletePillar(id: Long) = (APIAuthAction andThen PillarPermissionsCheck()).async { req =>
    implicit val username = Option(req.user.email)
    DeletePillarCommand(id).process().map { result =>
      result.fold[Result](NotFound)(_ => NoContent)
    } recover {
      commandErrorAsResult
    }
  }

  def listReferenceTypes() = APIAuthAction {
    Ok(Json.toJson(ExternalReferencesTypeRepository.loadAllReferenceTypes))
  }

  def checkPathInUse(tagType: String, slug: String, section: Option[Long], tagSubType: Option[String]) = APIAuthAction.async { req =>
    implicit val username = Option(req.user.email)
    new PathUsageCheck(tagType, slug, section, tagSubType).process().map{ result =>
      result.map{ t => Ok(Json.toJson(t)) } getOrElse BadRequest
    } recover {
      commandErrorAsResult
    }
  }

  def batchTag = APIAuthAction.async { req =>

    implicit val username = Option(req.user.email)
    req.body.asJson.map { json =>
      json.as[BatchTagCommand].process().map{ result =>
        result.map{t => NoContent } getOrElse NotFound
      } recover {
        commandErrorAsResult
      }
    }.getOrElse {
      Future.successful(BadRequest("Expecting Json data"))
    }
  }

  def mergeTag = (APIAuthAction andThen MergeTagPermissionsCheck()).async { req =>
    implicit val username = Option(req.user.email)
    req.body.asJson.map { json =>
      json.as[MergeTagCommand].process().map { result =>
        result.map{t => NoContent } getOrElse NotFound
      } recover {
        commandErrorAsResult
      }
    }.getOrElse {
      Future.successful(BadRequest("Expecting Json data"))
    }
  }

  def deleteTag(id: Long) = (APIHMACAuthAction andThen DeleteTagPermissionsCheck()).async { req =>
    implicit val username = Option(req.user.email)
    (DeleteTagCommand(id)).process().map { result =>
      result.map { t => NoContent } getOrElse NotFound
    } recover {
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

  def createSponsorship = (APIAuthAction andThen ManageSponsorshipsPermissionsCheck()).async { req =>
    implicit val username = Option(req.user.email)
    req.body.asJson.map { json =>
      json.as[CreateSponsorshipCommand].process().map { result =>
        result.map{t => Ok(Json.toJson(t)) } getOrElse NotFound
      } recover {
        commandErrorAsResult
      }
    }.getOrElse {
      Future.successful(BadRequest("Expecting Json data"))
    }
  }

  def updateSponsorship(id: Long) = (APIAuthAction andThen ManageSponsorshipsPermissionsCheck()).async { req =>
    implicit val username = Option(req.user.email)
    req.body.asJson.map { json =>
      json.as[UpdateSponsorshipCommand].process().map { result =>
        result.map{s => Ok(Json.toJson(DenormalisedSponsorship(s))) } getOrElse NotFound
      } recover {
        commandErrorAsResult
      }
    }.getOrElse {
      Future.successful(BadRequest("Expecting Json data"))
    }
  }

  def clashingSponsorships(id: Option[Long], tagIds: Option[String], sectionIds: Option[String], validFrom: Option[Long],
                           validTo: Option[Long], editions: Option[String]) = APIAuthAction.async { req =>
    implicit val username = Option(req.user.email)
    val editionSearch = editions.map(_.split(",").toList)
    val tagSearch: Option[List[Long]] = tagIds.map(_.split(",").toList.filter(_.length > 0).map(_.toLong))
    val sectionSearch: Option[List[Long]] = sectionIds.map(_.split(",").toList.filter(_.length > 0).map(_.toLong))
    new ClashingSponsorshipsFetch(id, tagSearch, sectionSearch, validFrom.map(new DateTime(_)), validTo.map(new DateTime(_)), editionSearch)
        .process().map { result =>
      result.map{ ss => Ok(Json.toJson(ss.map(DenormalisedSponsorship(_)))) } getOrElse BadRequest
    } recover {
      commandErrorAsResult
    }
  }

  def activeSponsorshipsForTag(id: Long) = APIAuthAction { req =>
    Ok(Json.toJson(SponsorshipRepository.searchSponsorships(SponsorshipSearchCriteria(tagId = Some(id), status = Some("active")))))
  }

  def activeSponsorshipsForSection(id: Long) = APIAuthAction { req =>
    Ok(Json.toJson(SponsorshipRepository.searchSponsorships(SponsorshipSearchCriteria(sectionId = Some(id), status = Some("active")))))
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

  def deleteJob(jobId: Long) = (APIAuthAction andThen JobDeletePermissionsCheck()) { req =>
    try {
      JobRepository.deleteIfTerminal(jobId)
      Ok
    } catch {
      case e: Exception => BadRequest("Job was not in a completed or failed state")
    }
  }

  def rollbackJob(id: Long) = (APIAuthAction andThen JobRollbackPermissionsCheck()) { req =>
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
