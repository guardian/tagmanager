package permissions

import com.gu.pandomainauth.action.UserRequest
import com.gu.editorial.permissions.client.{PermissionAuthorisation, PermissionDenied, PermissionGranted}
import play.api.mvc.{ActionFilter, Results}
import play.api.Logger
import scala.concurrent.{Future}
import scala.concurrent.ExecutionContext.Implicits.global

trait PermissionActionFilter extends ActionFilter[UserRequest] {
  val testAccess: String => Future[PermissionAuthorisation]
  val restrictedAction: String

  override def filter[A](request: UserRequest[A]) =
    testAccess(request.user.email).map {
      case PermissionGranted => None
      case PermissionDenied =>
        Logger.info(s"user not authorized to $restrictedAction")
        println("FAILED")
        Some(Results.Unauthorized)}
}

// Super Admin
object ReindexPermissionsCheck extends PermissionActionFilter {
  val testAccess: String => Future[PermissionAuthorisation] = Permissions.testUser(Permissions.TagSuperAdmin)
  val restrictedAction = "reindex"
}

object AddEditionToSectionPermissionsCheck extends PermissionActionFilter {
  val testAccess: String => Future[PermissionAuthorisation] = Permissions.testUser(Permissions.TagSuperAdmin)
  val restrictedAction = "add edition to section"
}

object RemoveEditionFromSectionPermissionsCheck extends PermissionActionFilter {
  val testAccess: String => Future[PermissionAuthorisation] = Permissions.testUser(Permissions.TagSuperAdmin)
  val restrictedAction = "remove edition from section"
}

object UpdateSectionPermissionsCheck extends PermissionActionFilter {
  val testAccess: String => Future[PermissionAuthorisation] = Permissions.testUser(Permissions.TagSuperAdmin)
  val restrictedAction = "update section"
}

// Admin
object MergeTagPermissionsCheck extends PermissionActionFilter {
  val testAccess: String => Future[PermissionAuthorisation] = Permissions.testUser(Permissions.TagAdmin)
  val restrictedAction = "merge tag"
}

object BatchTagPermissionsCheck extends PermissionActionFilter {
  val testAccess: String => Future[PermissionAuthorisation] = Permissions.testUser(Permissions.TagAdmin)
  val restrictedAction = "batch tag"
}

object DeleteTagPermissionsCheck extends PermissionActionFilter {
  val testAccess: String => Future[PermissionAuthorisation] = Permissions.testUser(Permissions.TagAdmin)
  val restrictedAction = "delete tag"
}
