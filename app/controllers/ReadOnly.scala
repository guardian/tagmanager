package controllers

import helpers.XmlHelpers._
import play.api.mvc.{Action, Controller}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import model.{Tag, Section, Delete, Merge, Create}
import repositories._

import scala.xml.Node

object ReadOnlyApi extends Controller {
  def getTagsAsXml() = Action {
    val tags = TagLookupCache.allTags

    val sections: Map[Long, Section] = SectionRepository.loadAllSections.map(s => s.id -> s)(collection.breakOut)
    val xmlTags = tags.get.sortBy(_.id).map(_.asExportedXml(sections))

    Ok(<tags>
      {xmlTags.seq.map { x => x }}
      </tags>)
  }

  def getSectionsAsXml() = Action {
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
      {xmlSections.seq.map { x => x }}
    </sections>)
  }

  def tagAsXml(id: Long) = Action {

    val sections: Map[Long, Section] = SectionRepository.loadAllSections.map(s => s.id -> s)(collection.breakOut)


    TagRepository.getTag(id).map { tag =>
      Ok(<tags>
        {tag.asExportedXml(sections)}
      </tags>)
    }.getOrElse(NotFound)
  }

  def mergesAsXml(since: Long) = Action {
    val merges = TagAuditRepository.getMerges.map { job =>
      Merge(job)
    }.filter(_.date.getMillis > since)
    Ok(
      <merges>
        {merges.map(x => x.asExportedXml)}
      </merges>
    )
  }

  def deletesAsXml(since: Long) = Action {
    val deletes = TagAuditRepository.getDeletes.map { audit =>
      Delete(audit)
    }.filter(_.date.getMillis > since)

    Ok(
      <deletes>
        {deletes.map(x => x.asExportedXml)}
      </deletes>
    )
  }

  def createsAsXml(since: Long) = Action {
    val audits = TagAuditRepository.getCreates.map { audit =>
      Create(audit.tagId, audit.date)
    }.filter(_.date.getMillis > since).sortBy(_.date.getMillis)


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
    val sections: Map[Long, Section] = SectionRepository.loadAllSections.map(s => s.id -> s)(collection.breakOut)

    val ret = tags.foldLeft(root: Node)((x, parent) => addChild(x, parent.asExportedXml(sections)))
    Ok(ret)
  }

  def modifiedAsXml(since: Long) = Action {
    import helpers.XmlHelpers._
    import scala.xml.Node

    val audits = TagAuditRepository.lastModifiedTags(since).sortBy(_.date.getMillis)

    val beginning = audits.headOption.map(_.date.toString("yyyy-MM-dd'T'HH:mm:ss.SSS"))
    val end =  audits.lastOption.map(_.date.toString("yyyy-MM-dd'T'HH:mm:ss.SSS"))

    val dateRange: Option[String] = (beginning, end) match {
      case (Some(beginning), Some(end)) => Some(s"${beginning}/${end}")
      case (Some(beginning), _) => Some(s"${beginning}/${beginning}")
      case (_, Some(end)) => Some(s"${end}/${end}")
      case (_ , _) => None
    }

    val sections: Map[Long, Section] = SectionRepository.loadAllSections.map(s => s.id -> s)(collection.breakOut)
    val tags = audits.map(x => TagRepository.getTag(x.tagId)).flatten
    val root = createElem("tags") % createAttribute("dateRange", dateRange)

    val ret = tags.foldLeft(root: Node)((x, parent) => addChild(x, parent.asExportedXml(sections)))
    Ok(ret)
  }

}
