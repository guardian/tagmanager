package controllers

import play.api.mvc.{Action, Controller}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import model.jobs.Merge
import repositories._
import play.api.libs.json._

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
    val merges = JobRepository.getMerges().map { job =>
      Merge(job)
    }

    Ok(<merges>
      {merges.map( x => x.asExportedXml)}
      </merges>
    )
  }
}
