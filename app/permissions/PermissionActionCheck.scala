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

object BatchTagPermissionsCheck extends PermissionActionFilter {
  val testAccess: String => Future[PermissionAuthorisation] = Permissions.testUser(Permissions.BatchTag)
  val restrictedAction = "batch tag"
}