package controllers

import model.command.CommandError._
import model.command.{ReindexSectionsCommand, ReindexTagsCommand}
import play.api.mvc.{Action, Controller}
import repositories.ReindexProgressRepository

import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future

object Reindex extends Controller {
  def reindexTags = Action.async { req =>
    ReindexProgressRepository.isTagReindexInProgress.flatMap { reindexing =>
      if (reindexing) {
        Future.successful(Forbidden)
      } else {
        ReindexTagsCommand().process.map { result =>
          result.map { count => Ok } getOrElse InternalServerError
        }
      }
    } recover {
      commandErrorAsResult
    }
  }

  def reindexSections = Action.async { req =>
    ReindexProgressRepository.isSectionReindexInProgress.flatMap { reindexing =>
      if (reindexing) {
        Future.successful(Forbidden)
      } else {
        ReindexSectionsCommand().process.map { result =>
          result.map { count => Ok } getOrElse InternalServerError
        }
      }
    } recover {
      commandErrorAsResult
    }
  }

  def getTagReindexProgress = Action.async { req =>
    ReindexProgressRepository.getTagReindexProgress.map{ maybeProgress =>
      maybeProgress.map { progress =>
        Ok(progress.toCapiForm().toJson)
      } getOrElse NotFound
    } recover {
      commandErrorAsResult
    }
  }

  def getSectionReindexProgress = Action.async { req =>
    ReindexProgressRepository.getSectionReindexProgress.map{ maybeProgress =>
      maybeProgress.map { progress =>
        Ok(progress.toCapiForm().toJson)
      } getOrElse NotFound
    } recover {
      commandErrorAsResult
    }
  }
}
