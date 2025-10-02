package controllers

import com.gu.pandomainauth.PanDomainAuthSettingsRefresher
import model.ClientConfig
import play.api.libs.json.Json
import play.api.mvc.{BaseController, ControllerComponents}
import play.api.Logging
import services.Config
import com.gu.tagmanagement.{KeywordType, TagType}
import permissions._
import play.api.libs.ws.WSClient

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

class App(
  val wsClient: WSClient,
  override val controllerComponents: ControllerComponents,
  val panDomainSettings: PanDomainAuthSettingsRefresher
)(
  implicit ec: ExecutionContext
)
  extends BaseController
  with PanDomainAuthActions
  with Logging {

  def index(id: String = "") = AuthAction.async { req =>

    val jsFileName = "build/app.js"

    val jsLocation = sys.env.get("JS_ASSET_HOST") match {
      case Some(assetHost) => assetHost + jsFileName
      case None => routes.Assets.versioned(jsFileName).toString
    }

    val userPermissions = Permissions.getPermissionsForUser(req.user.email)

    val allTags = TagType.list.map(_.name)
    var permittedTags = ListBuffer[String]()

    val allKeywordTypes = KeywordType.list.map(_.name)
    //TODO: add permissions for keyword types if required
    // var permittedKeywords = ListBuffer[String]()

    for (tag <- allTags) {
      TagTypePermissionMap(tag) match {
        case Some(permissionDefinition) =>
          if (userPermissions.get(permissionDefinition.name).contains(true)) {
            permittedTags += tag
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
      permittedKeywordTypes = allKeywordTypes,
      permissions = userPermissions,
      reauthUrl = "/reauth",
      tagSearchPageSize = Config().tagSearchPageSize
    )

    val result = views.html.Application.app("Tag Manager", jsLocation, Json.toJson(clientConfig).toString())
    Future.successful(Ok(result))
  }

}
