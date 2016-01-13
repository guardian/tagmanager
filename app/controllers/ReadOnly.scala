package controllers

import play.api.mvc.{Action, Controller}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

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
}
