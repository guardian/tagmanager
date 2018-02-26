package controllers

import net.logstash.logback.marker.Markers
import play.api
import services.Config
import scala.collection.JavaConverters._
import scala.concurrent.Future
import play.api.mvc._
import scala.util.matching.Regex

import play.api.libs.concurrent.Execution.Implicits._

object PerfAction {
  lazy val routes = router.Routes.documentation.map { value =>
    val verb = value._1
    val route = value._2

    (verb, new Regex("^" + route.replaceAll("""\$.*?<(.*?\+)>""", "$1") + "$")) -> route
  }
}

case class PerfAction[A](action: Action[A]) extends Action[A] {
  private def getRoute(req: Request[A]): String = {
    PerfAction.routes.find {
      case ((verb, regex), _) =>
        verb == req.method && regex.findAllMatchIn(req.path).hasNext
    }.map(_._2).getOrElse(req.path)
  }

  def apply(request: Request[A]): Future[Result] = {
    val start = System.currentTimeMillis

    action(request).map { result =>
      val end = System.currentTimeMillis
      val latency = end - start

      val route = getRoute(request)
      println(route)
      val markers = Markers.appendEntries(
        Map[String, Any](
          "app" -> Config().aws.app,
          "stack" -> Config().aws.stack,
          "stage" -> Config().aws.stage,
          "latency" -> latency.toString,
          "status" -> result.header.status,
          "method" -> request.method,
          "route" -> route
        ).asJava
      )

      api.Logger.logger.info(markers, "performance monitoring")

      result
    }
  }

  lazy val parser = action.parser
}
