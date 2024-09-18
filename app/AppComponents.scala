import com.gu.pandomainauth.{PanDomainAuthSettingsRefresher, S3BucketLoader}
import controllers.AssetsComponents
import model.jobs.JobRunner
import modules.clustersync.ClusterSynchronisation
import modules.sponsorshiplifecycle.SponsorshipLifecycleJobs
import play.api.ApplicationLoader.Context
import play.api.BuiltInComponentsFromContext
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.mvc.EssentialFilter
import play.api.routing.Router
import play.filters.HttpFiltersComponents
import router.Routes
import services._

import scala.language.postfixOps

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

  val panDomainSettings = PanDomainAuthSettingsRefresher(
    domain = config.pandaDomain,
    system = config.pandaSystemIdentifier,
    S3BucketLoader.forAwsSdkV1(AWS.S3Client, "pan-domain-auth-settings")
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
