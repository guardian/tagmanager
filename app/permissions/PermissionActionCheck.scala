package permissions

import com.gu.permissions.PermissionDefinition
import com.gu.pandomainauth.action.UserRequest
import play.api.Logging
import play.api.mvc.{ActionFilter, Results}

import scala.concurrent.{Future, ExecutionContext}

trait PermissionActionFilter extends ActionFilter[UserRequest] with Logging {
  val testAccess: String => Future[Boolean]
  val restrictedAction: String

  override def filter[A](request: UserRequest[A]) =
    if(request.user.email == "hmac-authed-service") {
      Future.successful(None)
    } else {
      testAccess(request.user.email).map {
        case true => None
        case false => logger.info(s"user not authorized to $restrictedAction")
          Some(Results.Unauthorized)
      }(executionContext)
    }
}
abstract class BasePermissionCheck(
                                    val permission: PermissionDefinition,
                                    val restrictedAction: String
                                  )(implicit val executionContext: ExecutionContext) extends PermissionActionFilter {

  val testAccess: String => Future[Boolean] =
    email => Future.successful(Permissions.testUser(email)(permission))
}

// Tag Edit
case class CreateTagPermissionsCheck()(implicit executionContext: ExecutionContext)
  extends BasePermissionCheck(Permissions.TagEdit, "create tag")

case class UpdateTagPermissionsCheck()(implicit executionContext: ExecutionContext)
  extends BasePermissionCheck(Permissions.TagEdit, "update tag")

// Tag Admin

case class AddEditionToSectionPermissionsCheck()(implicit executionContext: ExecutionContext)
  extends BasePermissionCheck(Permissions.TagAdmin, "add edition to section")

case class RemoveEditionFromSectionPermissionsCheck()(implicit executionContext: ExecutionContext)
  extends BasePermissionCheck(Permissions.TagAdmin, "remove edition from section")

case class DeleteTagPermissionsCheck()(implicit executionContext: ExecutionContext)
  extends BasePermissionCheck(Permissions.TagAdmin, "delete tag")

case class DeleteJobPermissionsCheck()(implicit executionContext: ExecutionContext)
  extends BasePermissionCheck(Permissions.TagAdmin, "delete job")

case class MergeTagPermissionsCheck()(implicit executionContext: ExecutionContext)
  extends BasePermissionCheck(Permissions.TagAdmin, "merge tag")

case class JobDeletePermissionsCheck()(implicit executionContext: ExecutionContext)
  extends BasePermissionCheck(Permissions.TagAdmin, "job delete")

case class JobRollbackPermissionsCheck()(implicit executionContext: ExecutionContext)
  extends BasePermissionCheck(Permissions.TagAdmin, "job rollback")

case class ModifySectionExpiryPermissionsCheck()(implicit executionContext: ExecutionContext)
  extends BasePermissionCheck(Permissions.TagAdmin,"trigger unexpiry of section content")

// Other Permissions
case class ManageSponsorshipsPermissionsCheck()(implicit executionContext: ExecutionContext)
  extends BasePermissionCheck(Permissions.CommercialTags, "manage sponsorships")

case class TriggerMigrationPermissionsCheck()(implicit executionContext: ExecutionContext)
  extends BasePermissionCheck(Permissions.TagAdmin, "trigger migration")

case class PillarPermissionsCheck()(implicit executionContext: ExecutionContext)
  extends BasePermissionCheck(Permissions.TagAdmin, "manage pillars")