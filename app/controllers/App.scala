package controllers

import play.api.mvc.{Action, Controller}
import repositories.{TagSearchCriteria, TagRepository}

object App extends Controller {

  def hello = Action{
    Ok(views.html.Application.hello("Hello world"))
  }

  def testScan = Action { req =>

    val criteria = TagSearchCriteria(
      q = req.getQueryString("q"),
      types = req.getQueryString("types").map(_.split(",").toList)
    )

    val startTime = System.currentTimeMillis
    val paths = TagRepository.search(criteria)
    val time = System.currentTimeMillis - startTime

    paths foreach println
    Ok(s"scan complete ${paths.size} items in $time ms")
  }

}
