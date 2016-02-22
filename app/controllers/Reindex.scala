package controllers

import model.command.CommandError._
import model.command.{ReindexSectionsCommand, ReindexTagsCommand}
import permissions.ReindexPermissionsCheck
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.Logger
import play.api.mvc.Controller

case class ExpectedItemCount(expectedItemCount: Int)

object ExpectedItemCount {
  implicit val expectedItemCountFormat: Format[ExpectedItemCount] = (
    JsPath \ "expectedItemCount"
  ).format[Int].inmap(count => ExpectedItemCount(count), (itemsCount: ExpectedItemCount) => itemsCount.expectedItemCount)
}


object Reindex extends Controller with PanDomainAuthActions {
  def reindexTags = (APIAuthAction andThen ReindexPermissionsCheck) { req =>
    // Get the reindex id provided by CAPI
    try {
      ReindexTagsCommand().process.map{ count =>
        Ok(Json.toJson(ExpectedItemCount(count)))
      } getOrElse InternalServerError
    } catch {
      commandErrorAsResult
    }
  }

  def reindexSections = (APIAuthAction andThen ReindexPermissionsCheck) { req =>
    try {
      ReindexSectionsCommand().process.map{ count =>
        Ok(Json.toJson(ExpectedItemCount(count)))
      } getOrElse InternalServerError
    } catch {
      commandErrorAsResult
    }
  }
}
