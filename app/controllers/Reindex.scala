package controllers

import model.command.CommandError._
import model.command.ReindexCommand
import permissions.ReindexPermissionsCheck
import play.api.libs.json._
import play.api.Logger
import play.api.mvc.Controller

object Reindex extends Controller with PanDomainAuthActions {
  def reindex = (APIAuthAction andThen ReindexPermissionsCheck) { req =>
    // Get the reindex id provided by CAPI
    req.body.asJson.map { json =>
        try {
          ReindexCommand((json \ "id").as[String]).process.map{t => NoContent } getOrElse InternalServerError
        } catch {
          commandErrorAsResult
        }
      }.getOrElse {
        BadRequest("Expecting reindex id in body")
      }

  }
}
