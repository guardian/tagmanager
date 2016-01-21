package controllers

import play.api.mvc.{Action, Controller}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import model.{Tag, Delete, Merge}
import repositories._

object ReadOnlyApi extends Controller {
  def getTagsAsXml() = Action.async {
    Future(TagLookupCache.allTags.get.map(_.asExportedXml)) map { tags =>
      Ok(<tags>
        {tags.seq.map { x => x }}
        </tags>)
    }
  }

  def tagAsXml(id: Long) = Action {
    TagRepository.getTag(id).map { tag =>
      Ok(tag.asExportedXml)
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

    val tags = audits.map(x => TagRepository.getTag(x.tagId)).flatten
    val root = createElem("tags") % createAttribute("dateRange", dateRange)

    val ret = tags.foldLeft(root: Node)((x, parent) => addChild(x, parent.asExportedXml))
    Ok(ret)
  }

}
