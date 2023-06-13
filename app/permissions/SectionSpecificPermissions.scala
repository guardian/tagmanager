package permissions

import com.gu.pandomainauth.action.UserRequest
import com.gu.permissions.PermissionDefinition
import play.api.mvc.{ActionFilter, AnyContent, Result, Results}

import scala.concurrent.{Future, ExecutionContext}

object SectionPermissionMap {
  def apply(isMicrosite: Boolean): Option[PermissionDefinition] = {
    isMicrosite match {
      case true => None
      case _ => Some(Permissions.TagAdmin)
    }
  }
}

trait SectionSpecificPermissionActionFilter extends ActionFilter[UserRequest] {

  val testAccess: PermissionDefinition => String => Boolean;
  val restrictedAction: String

  def commonTestAccess: PermissionDefinition => String => Boolean =
    Permissions.testUser

  override def filter[A](request: UserRequest[A]): Future[Option[Result]] = {
    request.body match {
      case b: AnyContent => {
        b.asJson.map { json =>
          val isMicrosite = (json \ "isMicrosite").as[Boolean]
          val permission = SectionPermissionMap(isMicrosite).getOrElse { return Future.successful(None) }

          val hasAccess = testAccess(permission)(request.user.email)

          if (hasAccess) {
            return Future.successful(None)
          } else {
            return Future.successful(Some(Results.Unauthorized))
          }
        }.getOrElse {
          Future.successful(Some(Results.BadRequest("Expecting Json data")))
        }
      }
      case _ => Future.successful(Some(Results.BadRequest("Expecting body content")))
    }
  }
}

case class UpdateSectionPermissionsCheck()(implicit val executionContext: ExecutionContext) extends SectionSpecificPermissionActionFilter {
  val testAccess: PermissionDefinition => String => Boolean = commonTestAccess
  val restrictedAction = "update section"
}

case class CreateSectionPermissionsCheck()(implicit val executionContext: ExecutionContext) extends SectionSpecificPermissionActionFilter {
  val testAccess: PermissionDefinition => String => Boolean = commonTestAccess
  val restrictedAction = "create section"
}
