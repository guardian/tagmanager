package permissions

import com.gu.editorial.permissions.client.{PermissionAuthorisation, PermissionDenied, PermissionGranted}
import com.gu.pandomainauth.action.UserRequest
import play.api.Logging
import play.api.mvc.{ActionFilter, Results}

import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future

trait PermissionActionFilter extends ActionFilter[UserRequest] with Logging {
  val testAccess: String => Future[PermissionAuthorisation]
  val restrictedAction: String

  override def filter[A](request: UserRequest[A]) =
    if(request.user.email == "hmac-authed-service") {
      Future.successful(None)
    } else {
      testAccess(request.user.email).map {
        case PermissionGranted => None
        case PermissionDenied =>
          logger.info(s"user not authorized to $restrictedAction")
          Some(Results.Unauthorized)}
    }
}

// Tag Edit
object CreateTagPermissionsCheck extends PermissionActionFilter {
  val testAccess: String => Future[PermissionAuthorisation] = Permissions.testUser(Permissions.TagEdit)
  val restrictedAction = "create tag"
}

object UpdateTagPermissionsCheck extends PermissionActionFilter {
  val testAccess: String => Future[PermissionAuthorisation] = Permissions.testUser(Permissions.TagEdit)
  val restrictedAction = "update tag"
}

// Tag Admin
object AddEditionToSectionPermissionsCheck extends PermissionActionFilter {
  val testAccess: String => Future[PermissionAuthorisation] = Permissions.testUser(Permissions.TagAdmin)
  val restrictedAction = "add edition to section"
}

object RemoveEditionFromSectionPermissionsCheck extends PermissionActionFilter {
  val testAccess: String => Future[PermissionAuthorisation] = Permissions.testUser(Permissions.TagAdmin)
  val restrictedAction = "remove edition from section"
}

object DeleteTagPermissionsCheck extends PermissionActionFilter {
  val testAccess: String => Future[PermissionAuthorisation] = Permissions.testUser(Permissions.TagAdmin)
  val restrictedAction = "delete tag"
}

object DeleteJobPermissionsCheck extends PermissionActionFilter {
  val testAccess: String => Future[PermissionAuthorisation] = Permissions.testUser(Permissions.TagAdmin)
  val restrictedAction = "delete job"
}

object MergeTagPermissionsCheck extends PermissionActionFilter {
  val testAccess: String => Future[PermissionAuthorisation] = Permissions.testUser(Permissions.TagAdmin)
  val restrictedAction = "merge tag"
}

object JobDeletePermissionsCheck extends PermissionActionFilter {
  val testAccess: String => Future[PermissionAuthorisation] = Permissions.testUser(Permissions.TagAdmin)
  val restrictedAction = "job delete"
}

object JobRollbackPermissionsCheck extends PermissionActionFilter {
  val testAccess: String => Future[PermissionAuthorisation] = Permissions.testUser(Permissions.TagAdmin)
  val restrictedAction = "job rollback"
}

object ModifySectionExpiryPermissionsCheck extends PermissionActionFilter {
  val testAccess: String => Future[PermissionAuthorisation] = Permissions.testUser(Permissions.TagAdmin)
  val restrictedAction = "trigger unexpiry of section content"
}

object ManageSponsorshipsPermissionsCheck extends PermissionActionFilter {
  val testAccess: String => Future[PermissionAuthorisation] = Permissions.testUser(Permissions.CommercialTags)
  val restrictedAction = "manage sponsorships"
}

object TriggerMigrationPermissionsCheck extends PermissionActionFilter {
  val testAccess: String => Future[PermissionAuthorisation] = Permissions.testUser(Permissions.TagAdmin)
  val restrictedAction = "manage sponsorships"
}

object PillarPermissionsCheck extends PermissionActionFilter {
  val testAccess: String => Future[PermissionAuthorisation] = Permissions.testUser(Permissions.TagAdmin)
  val restrictedAction = "manage pillars"
}
