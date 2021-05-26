package controllers

import com.gu.pandomainauth.PanDomainAuthSettingsRefresher
import play.api.Logging
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.ws.WSClient

class Login(
  val wsClient: WSClient,
  override val controllerComponents: ControllerComponents,
  val panDomainSettings: PanDomainAuthSettingsRefresher
)(
  implicit ec: ExecutionContext
)
  extends BaseController
  with PanDomainAuthActions
  with Logging {

  def reauth = AuthAction {
    Ok("auth ok")
  }

  def oauthCallback = Action.async { implicit request =>
    processOAuthCallback()
  }

  def logout = Action.async { implicit request =>
    Future(processLogout)
  }
}
