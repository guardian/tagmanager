import com.gu.pandomainauth.PanDomainAuthSettingsRefresher
import play.api.ApplicationLoader.Context
import play.api.BuiltInComponentsFromContext
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.routing.Router
import play.filters.HttpFiltersComponents
import router.Routes
import services._

class AppComponents(context: Context, config: Config)
  extends BuiltInComponentsFromContext(context)
  with AhcWSComponents
  with HttpFiltersComponents {

  val panDomainSettings = new PanDomainAuthSettingsRefresher(
    domain = config.pandaDomain,
    system = config.pandaSystemIdentifier,
    bucketName = config.pandaBucketName,
    settingsFileKey= config.pandaSettingsFileKey,
    s3Client = AWS.S3Client,
  )


  lazy val router: Router = new Routes(
    httpErrorHandler,
    new controllers.App(wsClient, controllerComponents, panDomainSettings),
    new controllers.TagManagementApi(wsClient, controllerComponents, panDomainSettings)
  )

}
