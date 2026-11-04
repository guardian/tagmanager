package controllers

import com.gu.pandomainauth.PanDomainAuthSettingsRefresher
import model.command.CommandError._
import model.command.{ReindexPillarsCommand, ReindexSectionsCommand, ReindexTagsCommand}
import play.api.Configuration
import play.api.Logging
import play.api.mvc.{ActionBuilder, Request, AnyContent, Result, BodyParser, BaseController, ControllerComponents}
import repositories.ReindexProgressRepository
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

class Reindex(
  val wsClient: WSClient,
  override val controllerComponents: ControllerComponents,
  reindexApiKey: String,
)(
  implicit ec: ExecutionContext
)
  extends BaseController
  with Logging {

     private object ApiKeyAction extends ActionBuilder[Request, AnyContent] {

       def invokeBlock[A](request: Request[A], handle: Request[A] => Future[Result]): Future[Result] = {
         if(request.getQueryString("api").contains(reindexApiKey))
           handle(request)
         else
           Future.successful(Unauthorized(""))
       }

       override def parser: BodyParser[AnyContent] = controllerComponents.parsers.defaultBodyParser

       override protected def executionContext: ExecutionContext = controllerComponents.executionContext
     }

  def reindexTags = ApiKeyAction.async { req =>
    implicit val username: Option[String] = None // unfortunately we don't have a username available
    ReindexProgressRepository.isTagReindexInProgress.flatMap { reindexing =>
      if (reindexing) {
        Future.successful(Forbidden)
      } else {
        ReindexTagsCommand().process().map { result =>
          result.map { count => Ok } getOrElse InternalServerError
        }
      }
    } recover {
      commandErrorAsResult
    }
  }

  def reindexSections = ApiKeyAction.async { req =>
    implicit val username: Option[String] = None // unfortunately we don't have a username available
    ReindexProgressRepository.isSectionReindexInProgress.flatMap { reindexing =>
      if (reindexing) {
        Future.successful(Forbidden)
      } else {
        ReindexSectionsCommand().process().map { result =>
          result.map { count => Ok } getOrElse InternalServerError
        }
      }
    } recover {
      commandErrorAsResult
    }
  }

  def reindexPillars = ApiKeyAction.async { req =>
    implicit val username: Option[String] = None // unfortunately we don't have a username available
    ReindexProgressRepository.isPillarReindexInProgress.flatMap { reindexing =>
      if (reindexing) {
        Future.successful(Forbidden)
      } else {
        ReindexPillarsCommand().process().map { result =>
          result.map { count => Ok } getOrElse InternalServerError
        }
      }
    } recover {
      commandErrorAsResult
    }
  }

  def getPillarReindexProgress = ApiKeyAction.async { req =>
    ReindexProgressRepository.getPillarReindexProgress.map { maybeProgress =>
      maybeProgress.map { progress =>
        Ok(progress.toCapiForm().toJson)
      } getOrElse NotFound
    } recover {
      commandErrorAsResult
    }
  }

  def getTagReindexProgress = ApiKeyAction.async { req =>
    ReindexProgressRepository.getTagReindexProgress.map{ maybeProgress =>
      maybeProgress.map { progress =>
        Ok(progress.toCapiForm().toJson)
      } getOrElse NotFound
    } recover {
      commandErrorAsResult
    }
  }

  def getSectionReindexProgress = ApiKeyAction.async { req =>
    ReindexProgressRepository.getSectionReindexProgress.map{ maybeProgress =>
      maybeProgress.map { progress =>
        Ok(progress.toCapiForm().toJson)
      } getOrElse NotFound
    } recover {
      commandErrorAsResult
    }
  }
}
