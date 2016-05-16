package controllers

import model.ClientConfig
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import repositories.{TagSearchCriteria, TagRepository}
import play.api.Logger
import services.Config
import com.gu.tagmanagement.{TagType}
import permissions._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable.ListBuffer

object App extends Controller with PanDomainAuthActions {

  def index(id: String = "") = AuthAction.async { req =>

    val jsFileName = "build/app.js"

    val jsLocation = sys.env.get("JS_ASSET_HOST") match {
      case Some(assetHost) => assetHost + jsFileName
      case None => routes.Assets.versioned(jsFileName).toString
    }

    Permissions.getPermissionsForUser(req.user.email).map { permissions =>

      val allTags = TagType.list.map(_.name)
      var permittedTags = ListBuffer[String]()

      for (tag <- allTags) {
        TagTypePermissionMap(tag) match {
          case Some(p) => {
            permissions.get(p.name).map { hasPermission =>
              if (hasPermission) {
                permittedTags += tag
              }
            }
          }
          case None => permittedTags += tag
        }
      }

      val clientConfig = ClientConfig(
        username = req.user.email,
        capiUrl = Config().capiUrl,
        capiPreviewUrl = "/support/previewCapi",
        capiKey = Config().capiKey,
        tagTypes = allTags,
        permittedTagTypes = permittedTags.toList,
        permissions = permissions,
        reauthUrl = "/reauth"
      )

      Ok(views.html.Application.app("Tag Manager", jsLocation, Json.toJson(clientConfig).toString()))
    }

  }

  def hello = AuthAction {
    Logger.info("saying hello")
    Ok(views.html.Application.hello("Hello world"))
  }

  def testScan = Action { req =>

    val criteria = TagSearchCriteria(
      q = req.getQueryString("q"),
      types = req.getQueryString("types").map(_.split(",").toList)
    )

    val startTime = System.currentTimeMillis
    val paths = TagRepository.scanSearch(criteria)
    val time = System.currentTimeMillis - startTime

    paths foreach println
    Ok(s"scan complete ${paths.size} items in $time ms")
  }

}
