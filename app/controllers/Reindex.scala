package controllers

import model.command.CommandError._
import model.command.{ReindexSectionsCommand, ReindexTagsCommand}
import permissions.ReindexPermissionsCheck
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.Logger
import play.api.mvc.{Action, Controller}
import repositories.ReindexProgressRepository

object Reindex extends Controller {
  def reindexTags = Action { req =>
    // Get the reindex id provided by CAPI
    try {
      if (ReindexProgressRepository.isTagReindexInProgress) {
        Forbidden
      } else {
        ReindexTagsCommand().process.map{ count =>
          Ok
        } getOrElse InternalServerError
      }
    } catch {
      commandErrorAsResult
    }
  }

  def reindexSections = Action { req =>
    try {
      if (ReindexProgressRepository.isSectionReindexInProgress) {
        Forbidden
      } else {
        ReindexSectionsCommand().process.map{ count =>
          Ok
        } getOrElse InternalServerError
      }
    } catch {
      commandErrorAsResult
    }
  }

  def getTagReindexProgress = Action { req =>
    try {
      ReindexProgressRepository.getTagReindexProgress.map { progress =>
        Ok(progress.toCapiForm().toJson)
      } getOrElse NotFound
    } catch {
      commandErrorAsResult
    }
  }

  def getSectionReindexProgress = Action { req =>
    try {
      ReindexProgressRepository.getSectionReindexProgress.map { progress =>
        Ok(progress.toCapiForm().toJson)
      } getOrElse NotFound
    } catch {
      commandErrorAsResult
    }
  }
}
