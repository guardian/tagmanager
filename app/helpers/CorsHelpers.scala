package helpers
import play.api.mvc.{Action, Request, Result}

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._


case class CORSable[A](origins: String*)(action: Action[A]) extends Action[A] {
  def apply(request: Request[A]): Future[Result] = {
    val headers = request.headers.get("Origin").map { origin =>
      if(origins.contains(origin)) {
        List(CORSable.CORS_ALLOW_ORIGIN -> origin, CORSable.CORS_CREDENTIALS -> "true")
      } else { Nil }
    }

    action(request).map(_.withHeaders(headers.getOrElse(Nil) :_*))
  }

  lazy val parser = action.parser
}

object CORSable {
  val CORS_ALLOW_ORIGIN = "Access-Control-Allow-Origin"
  val CORS_CREDENTIALS = "Access-Control-Allow-Credentials"
  val CORS_ALLOW_METHODS = "Access-Control-Allow-Methods"
  val CORS_ALLOW_HEADERS = "Access-Control-Allow-Headers"
}