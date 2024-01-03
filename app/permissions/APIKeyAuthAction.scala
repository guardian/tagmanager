package permissions

import com.gu.pandahmac.HMACAuthActions
import com.gu.pandomainauth.action.{AuthActions, UserRequest}
import com.gu.pandomainauth.model.NotAuthenticated
import play.api.mvc.{ActionBuilder, AnyContent, BaseController, BodyParser, Request, Result}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc.Results.Forbidden
import services.Config

trait APIKeyAuthActions extends BaseController {

  def config: Config

  object APIKeyAuthAction extends ActionBuilder[Request, AnyContent] {
    override def parser: BodyParser[AnyContent] = controllerComponents.parsers.default
    override protected def executionContext: ExecutionContext = controllerComponents.executionContext

    override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
      val apiKey = request.queryString.getOrElse("api-key", Nil).headOption
      if (apiKey.exists(config.reindexKeys.contains))
        block(request)
      else
        Future(Forbidden("Missing or invalid api-key"))
    }
  }
}