package controllers

import model.command.CommandError._
import model.command.{ReindexSectionsCommand, ReindexTagsCommand}
import permissions.ReindexPermissionsCheck
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.Logger
import play.api.mvc.Controller
import repositories.ReindexProgressRepository

object Reindex extends Controller with PanDomainAuthActions {
  def reindexTags = (APIAuthAction andThen ReindexPermissionsCheck) { req =>
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

  def reindexSections = (APIAuthAction andThen ReindexPermissionsCheck) { req =>
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

  def getTagReindexProgress = (APIAuthAction andThen ReindexPermissionsCheck) { req =>
    try {

      ReindexProgressRepository.getTagReindexProgress.map { progress =>
        Ok(progress.toCapiForm().toJson)
      } getOrElse NotFound
    } catch {
      commandErrorAsResult
    }
  }

  def getSectionReindexProgress = (APIAuthAction andThen ReindexPermissionsCheck) { req =>
    try {
      ReindexProgressRepository.getSectionReindexProgress.map { progress =>
        Ok(progress.toCapiForm().toJson)
      } getOrElse NotFound
    } catch {
      commandErrorAsResult
    }
  }
}
