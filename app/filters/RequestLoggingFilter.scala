package filters

import net.logstash.logback.marker.Markers.appendEntries
import org.apache.pekko.stream.Materializer
import play.api.mvc._
import play.api.{Logger, MarkerContext}

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success}

object RequestLoggingFilter {
  val requestIdHeader = "X-Request-Id"
}

class RequestLoggingFilter(implicit val mat: Materializer, ec: ExecutionContext)
    extends Filter {

  private val logger = Logger("request")

  def apply(
      next: RequestHeader => Future[Result]
  )(requestHeader: RequestHeader): Future[Result] = {
    val startTime = System.currentTimeMillis

    // Reuse an upstream request ID if there is one, so logs can be correlated
    val request =
      if (requestHeader.headers.hasHeader(RequestLoggingFilter.requestIdHeader))
        requestHeader
      else
        requestHeader.withHeaders(
          requestHeader.headers.replace(
            RequestLoggingFilter.requestIdHeader -> UUID.randomUUID().toString
          )
        )

    next(request).transform {
      case Success(result) =>
        val requestTime = System.currentTimeMillis - startTime
        log(request, Right(result), requestTime)
        Success(result.withHeaders("Request-Time" -> requestTime.toString))
      case Failure(err) =>
        log(request, Left(err), System.currentTimeMillis - startTime)
        Failure(err)
    }
  }

  private def log(
      request: RequestHeader,
      outcome: Either[Throwable, Result],
      requestTime: Long
  ): Unit = {
    val status = outcome.map(_.header.status).getOrElse(500)
    val message =
      s"${request.method} ${request.uri} took ${requestTime}ms and returned $status"

    val queryStringEntries = request.queryString.map { case (k, v) =>
      s"query_string.$k" -> v.mkString(", ")
    }
    val optionalEntries = Map(
      "referrer" -> request.headers.get("Referer"),
      "userAgent" -> request.headers.get("User-Agent"),
      "requestId" -> request.headers.get(RequestLoggingFilter.requestIdHeader)
    ).collect { case (k, Some(v)) => k -> v }

    val markerEntries = Map(
      "method" -> request.method,
      "url" -> request.uri,
      "path" -> request.path,
      "requestTime" -> requestTime,
      "status" -> status,
      // X-Forwarded-For is client-controllable, so also log the socket address
      "origin" -> request.headers
        .get("X-Forwarded-For")
        .getOrElse(request.remoteAddress),
      "remoteAddress" -> request.remoteAddress
    ) ++ optionalEntries ++ queryStringEntries
    implicit val markerContext: MarkerContext =
      MarkerContext(appendEntries(markerEntries.asJava))

    outcome match {
      case Left(err) => logger.error(message, err)
      case Right(_) if status < 200 || status >= 400 => logger.error(message)
      case Right(_)                                  => logger.info(message)
    }
  }
}
