package controllers

import play.api.mvc.{Action, Controller}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import repositories._

object ReadOnly extends Controller {
  def getTagsAsXml() = Action.async {
    Future(TagLookupCache.allTags.get.map(_.asXml)) map { tags =>
      Ok(<tags>
        {tags.seq.map { x => x }}
        </tags>)
    }
  }
}
