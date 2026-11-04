package controllers

import helpers.XmlHelpers._
import model.{Create, Delete, Merge, Section}
import play.api.Logging
import play.api.libs.ws.WSClient
import play.api.mvc.{BaseController, ControllerComponents}
import repositories._
import com.gu.pandahmac.HMACAuthActions
import com.gu.pandomainauth.PanDomainAuthSettingsRefresher
import com.gu.pandomainauth.model.AuthenticatedUser

import scala.concurrent.ExecutionContext
import scala.xml.Node

class ReadOnlyApi(
  val wsClient: WSClient,
  override val controllerComponents: ControllerComponents,
  override val panDomainSettings: PanDomainAuthSettingsRefresher
)(
  implicit ec: ExecutionContext
)
  extends BaseController
  with PanDomainAuthActions
  with Logging {

  def getTagsAsXml() = APIHMACAuthAction {
    val tags = TagLookupCache.allTags

    val sections: Map[Long, Section] = SectionRepository.loadAllSections.map(s => s.id -> s).toMap
    val xmlTags = tags.get.sortBy(_.id).map(_.asExportedXml(sections))

    Ok(<tags>
      {xmlTags.map { x => x }}
      </tags>)
  }

  def getSectionsAsXml() = APIHMACAuthAction {
    val sections = SectionRepository.loadAllSections

    //Lack of a Section in TagManager was previously represented as a "Global" section.
    val globalSection = new Section(
      name = "Global",
      id = 281,
      sectionTagId = 0,
      path = "",
      wordsForUrl = "",
      pageId = 0,
      isMicrosite = false
    )

    val xmlSections = sections.map(_.asExportedXml) ++ globalSection.asExportedXml


    Ok(<sections>
      {xmlSections.map { x => x }}
    </sections>)
  }

  def tagAsXml(id: Long) = APIHMACAuthAction {

    val sections: Map[Long, Section] = SectionRepository.loadAllSections.map(s => s.id -> s).toMap


    TagRepository.getTag(id).map { tag =>
      Ok(<tags>
        {tag.asExportedXml(sections)}
      </tags>)
    }.getOrElse(NotFound)
  }

  def mergesAsXml(since: Long) = APIHMACAuthAction {
    val merges = TagAuditRepository.getMerges.map { job =>
      Merge(job)
    }.filter(_.date.getMillis > since)
    Ok(
      <merges>
        {merges.map(x => x.asExportedXml)}
      </merges>
    )
  }

  def deletesAsXml(since: Long) = APIHMACAuthAction {
    val deletes = TagAuditRepository.getAuditsOfTagOperationsSince("deleted", since).map({
      audit => Delete(audit)
    }).sortBy(_.date.getMillis)

    Ok(
      <deletes>
        {deletes.map(x => x.asExportedXml)}
      </deletes>
    )
  }

  def createsAsXml(since: Long) = APIHMACAuthAction {
    val audits = TagAuditRepository.getAuditsOfTagOperationsSince("created", since).map({
      audit => Create(audit.tagId, audit.date)
    }).sortBy(_.date.getMillis)

    val beginning = audits.headOption.map(_.date.toString("yyyy-MM-dd'T'HH:mm:ss.SSS"))
    val end =  audits.lastOption.map(_.date.toString("yyyy-MM-dd'T'HH:mm:ss.SSS"))

    val dateRange: Option[String] = (beginning, end) match {
      case (Some(beginning), Some(end)) => Some(s"${beginning}/${end}")
      case (Some(beginning), _) => Some(s"${beginning}/${beginning}")
      case (_, Some(end)) => Some(s"${end}/${end}")
      case (_ , _) => None
    }

    val tags = audits.map(x => TagRepository.getTag(x.tagId)).flatten
    val root = createElem("tags") % createAttribute("dateRange", dateRange)
    val sections: Map[Long, Section] = SectionRepository.loadAllSections.map(s => s.id -> s).toMap

    val ret = tags.foldLeft(root: Node)((x, parent) => addChild(x, parent.asExportedXml(sections)))
    Ok(ret)
  }

  def modifiedAsXml(since: Long) = APIHMACAuthAction {
    import helpers.XmlHelpers._

    import scala.xml.Node

    val audits = TagAuditRepository.getAuditsOfTagOperationsSince("updated", since).sortBy(_.date.getMillis)

    val beginning = audits.headOption.map(_.date.toString("yyyy-MM-dd'T'HH:mm:ss.SSS"))
    val end =  audits.lastOption.map(_.date.toString("yyyy-MM-dd'T'HH:mm:ss.SSS"))

    val dateRange: Option[String] = (beginning, end) match {
      case (Some(beginning), Some(end)) => Some(s"${beginning}/${end}")
      case (Some(beginning), _) => Some(s"${beginning}/${beginning}")
      case (_, Some(end)) => Some(s"${end}/${end}")
      case (_ , _) => None
    }

    val sections: Map[Long, Section] = SectionRepository.loadAllSections.map(s => s.id -> s).toMap
    val tags = audits.map(x => TagRepository.getTag(x.tagId)).flatten
    val root = createElem("tags") % createAttribute("dateRange", dateRange)

    val ret = tags.foldLeft(root: Node)((x, parent) => addChild(x, parent.asExportedXml(sections)))
    Ok(ret)
  }

}
