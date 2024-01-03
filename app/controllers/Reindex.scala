package controllers

import model.command.CommandError._
import model.command.{ReindexPillarsCommand, ReindexSectionsCommand, ReindexTagsCommand}
import permissions.APIKeyAuthActions
import play.api.Logging
import play.api.mvc.{BaseController, ControllerComponents}
import repositories.ReindexProgressRepository
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}
import services.Config

class Reindex(
  val wsClient: WSClient,
  override val controllerComponents: ControllerComponents,
  override val config: Config
)(
  implicit ec: ExecutionContext
)
  extends BaseController
  with APIKeyAuthActions
  with Logging {

  def reindexTags = APIKeyAuthAction.async { req =>
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

  def reindexSections = APIKeyAuthAction.async { req =>
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

  def reindexPillars = APIKeyAuthAction.async { req =>
    ReindexProgressRepository.isPillarReindexInProgress.flatMap { reindexing =>
      if (reindexing) {
        Future.successful(Forbidden)
      } else {
        ReindexPillarsCommand().process.map { result =>
          result.map { count => Ok } getOrElse InternalServerError
        }
      }
    } recover {
      commandErrorAsResult
    }
  }

  def getPillarReindexProgress = APIKeyAuthAction.async { req =>
    ReindexProgressRepository.getPillarReindexProgress.map { maybeProgress =>
      maybeProgress.map { progress =>
        Ok(progress.toCapiForm().toJson)
      } getOrElse NotFound
    } recover {
      commandErrorAsResult
    }
  }

  def getTagReindexProgress = APIKeyAuthAction.async { req =>
    ReindexProgressRepository.getTagReindexProgress.map{ maybeProgress =>
      maybeProgress.map { progress =>
        Ok(progress.toCapiForm().toJson)
      } getOrElse NotFound
    } recover {
      commandErrorAsResult
    }
  }

  def getSectionReindexProgress = APIKeyAuthAction.async { req =>
    ReindexProgressRepository.getSectionReindexProgress.map{ maybeProgress =>
      maybeProgress.map { progress =>
        Ok(progress.toCapiForm().toJson)
      } getOrElse NotFound
    } recover {
      commandErrorAsResult
    }
  }
}
