import com.gu.editorial.permissions.client.PermissionsUser
import com.gu.pandomainauth.PanDomainAuthSettingsRefresher
import controllers.AssetsComponents
import model.jobs.JobRunner
import modules.clustersync.ClusterSynchronisation
import modules.sponsorshiplifecycle.SponsorshipLifecycleJobs
import permissions.Permissions
import play.api.ApplicationLoader.Context
import play.api.BuiltInComponentsFromContext
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.mvc.EssentialFilter
import play.api.routing.Router
import play.filters.HttpFiltersComponents
import router.Routes
import services._

class AppComponents(context: Context, config: Config)
  extends BuiltInComponentsFromContext(context)
  with AssetsComponents
  with AhcWSComponents
  with HttpFiltersComponents {

  override def httpFilters: Seq[EssentialFilter] =
    super.httpFilters
      .filterNot(allowedHostsFilter ==)
      .filterNot(csrfFilter ==)

  new ClusterSynchronisation(context.lifecycle)
  new JobRunner(context.lifecycle)
  new SponsorshipLifecycleJobs(context.lifecycle)

  // This is requried because there's an issue in the permissions client
  // which causes the permissions to come back as denied for the first person
  // to request them. Seems to be a timing bug...
  //
  // This should only be a temporary fix @ 2016/02/09
  Permissions.list(PermissionsUser("preload@permissions"))

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
    new controllers.TagManagementApi(wsClient, controllerComponents, panDomainSettings),
    new controllers.Reindex(wsClient, controllerComponents),
    new controllers.HyperMediaApi(wsClient, controllerComponents, panDomainSettings),
    new controllers.ReadOnlyApi(wsClient, controllerComponents),
    new controllers.Login(wsClient, controllerComponents, panDomainSettings),
    new controllers.Management(wsClient, controllerComponents),
    assets,
    new controllers.Support(wsClient, controllerComponents, panDomainSettings),
    new controllers.Migration(wsClient, controllerComponents, panDomainSettings)
  )

}
