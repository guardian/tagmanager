package controllers
import model.command.CommandError._
import model.command._
import model.jobs.{BatchTagAddCompleteCheck, Job}
import org.joda.time.DateTime
import permissions.Permissions
import play.api.Logger
import model.Tag
import model.Section
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import repositories._
import permissions._

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
    Ok(Json.toJson(SectionRepository.loadAllSections.filter(_.isMicrosite == false)))
  }

  def listMicrositeSections() = APIAuthAction {
    Ok(Json.toJson(SectionRepository.loadAllSections.filter(_.isMicrosite == true)))
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

  def batchTag = (APIAuthAction andThen BatchTagPermissionsCheck) { req =>

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

  def deleteTag(id: Long)= (APIAuthAction andThen DeleteTagPermissionsCheck) { req =>
    implicit val username = Option(s"${req.user.firstName} ${req.user.lastName}")
    try {
      (new DeleteTagCommand(id)).process.map{t => NoContent } getOrElse NotFound
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

}
