package permissions

import com.gu.editorial.permissions.client.{Permission, PermissionAuthorisation, PermissionDenied, PermissionGranted}
import com.gu.pandomainauth.action.UserRequest
import play.api.mvc.{ActionFilter, AnyContent, Result, Results}

import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future

object SectionPermissionMap {
  def apply(isMicrosite: Boolean): Option[Permission] = {
    isMicrosite match {
      case true => None
      case _ => Some(Permissions.TagAdmin)
    }
  }
}

trait SectionSpecificPermissionActionFilter extends ActionFilter[UserRequest] {

  val testAccess: (Permission => (String => Future[PermissionAuthorisation]))
  val restrictedAction: String

  override def filter[A](request: UserRequest[A]): Future[Option[Result]] = {
    request.body match {
      case b: AnyContent => {
        b.asJson.map { json =>
          val isMicrosite = (json \ "isMicrosite").as[Boolean]

          val permission = SectionPermissionMap(isMicrosite).getOrElse { return Future.successful(None) }

          testAccess(permission)(request.user.email).map {
            case PermissionGranted => None
            case PermissionDenied => Some(Results.Unauthorized)
          }
        }.getOrElse {
          Future.successful(Some(Results.BadRequest("Expecting Json data")))
        }
      }
      case _ => Future.successful(Some(Results.BadRequest("Expecting body content")))
    }
  }
}

object UpdateSectionPermissionsCheck extends SectionSpecificPermissionActionFilter {
  val testAccess: (Permission => (String => Future[PermissionAuthorisation])) = Permissions.testUser
  val restrictedAction = "update section"
}

object CreateSectionPermissionsCheck extends SectionSpecificPermissionActionFilter {
  val testAccess: (Permission => (String => Future[PermissionAuthorisation])) = Permissions.testUser
  val restrictedAction = "create section"
}
