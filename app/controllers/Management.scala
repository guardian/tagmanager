package controllers

import com.gu.tagmanager.BuildInfo
import play.api.Logging
import play.api.libs.ws.WSClient
import play.api.mvc.{BaseController, ControllerComponents}

import scala.concurrent.ExecutionContext


class Management(
  val wsClient: WSClient,
  override val controllerComponents: ControllerComponents
)(
  implicit ec: ExecutionContext
)
  extends BaseController
    with Logging {

  def manifest = Action { _ => Ok(BuildInfo.toJson) }

  def healthCheck = Action {
    Ok("OK")
  }
}
