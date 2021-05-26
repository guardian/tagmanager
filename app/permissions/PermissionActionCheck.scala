package permissions

import com.gu.editorial.permissions.client.{PermissionAuthorisation, PermissionDenied, PermissionGranted}
import com.gu.pandomainauth.action.UserRequest
import play.api.Logging
import play.api.mvc.{ActionFilter, Results}

import scala.concurrent.{Future, ExecutionContext}

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
          Some(Results.Unauthorized)}(executionContext)
    }
}

// Tag Edit
case class CreateTagPermissionsCheck()(implicit val executionContext: ExecutionContext) extends PermissionActionFilter {
  val testAccess: String => Future[PermissionAuthorisation] = Permissions.testUser(Permissions.TagEdit)
  val restrictedAction = "create tag"
}

case class UpdateTagPermissionsCheck()(implicit val executionContext: ExecutionContext) extends PermissionActionFilter {
  val testAccess: String => Future[PermissionAuthorisation] = Permissions.testUser(Permissions.TagEdit)
  val restrictedAction = "update tag"
}

// Tag Admin
case class AddEditionToSectionPermissionsCheck()(implicit val executionContext: ExecutionContext) extends PermissionActionFilter {
  val testAccess: String => Future[PermissionAuthorisation] = Permissions.testUser(Permissions.TagAdmin)
  val restrictedAction = "add edition to section"
}

case class RemoveEditionFromSectionPermissionsCheck()(implicit val executionContext: ExecutionContext) extends PermissionActionFilter {
  val testAccess: String => Future[PermissionAuthorisation] = Permissions.testUser(Permissions.TagAdmin)
  val restrictedAction = "remove edition from section"
}

case class DeleteTagPermissionsCheck()(implicit val executionContext: ExecutionContext) extends PermissionActionFilter {
  val testAccess: String => Future[PermissionAuthorisation] = Permissions.testUser(Permissions.TagAdmin)
  val restrictedAction = "delete tag"
}

case class DeleteJobPermissionsCheck()(implicit val executionContext: ExecutionContext) extends PermissionActionFilter {
  val testAccess: String => Future[PermissionAuthorisation] = Permissions.testUser(Permissions.TagAdmin)
  val restrictedAction = "delete job"
}

case class MergeTagPermissionsCheck()(implicit val executionContext: ExecutionContext) extends PermissionActionFilter {
  val testAccess: String => Future[PermissionAuthorisation] = Permissions.testUser(Permissions.TagAdmin)
  val restrictedAction = "merge tag"
}

case class JobDeletePermissionsCheck()(implicit val executionContext: ExecutionContext) extends PermissionActionFilter {
  val testAccess: String => Future[PermissionAuthorisation] = Permissions.testUser(Permissions.TagAdmin)
  val restrictedAction = "job delete"
}

case class JobRollbackPermissionsCheck()(implicit val executionContext: ExecutionContext) extends PermissionActionFilter {
  val testAccess: String => Future[PermissionAuthorisation] = Permissions.testUser(Permissions.TagAdmin)
  val restrictedAction = "job rollback"
}

case class ModifySectionExpiryPermissionsCheck()(implicit val executionContext: ExecutionContext) extends PermissionActionFilter {
  val testAccess: String => Future[PermissionAuthorisation] = Permissions.testUser(Permissions.TagAdmin)
  val restrictedAction = "trigger unexpiry of section content"
}

case class ManageSponsorshipsPermissionsCheck()(implicit val executionContext: ExecutionContext) extends PermissionActionFilter {
  val testAccess: String => Future[PermissionAuthorisation] = Permissions.testUser(Permissions.CommercialTags)
  val restrictedAction = "manage sponsorships"
}

case class TriggerMigrationPermissionsCheck()(implicit val executionContext: ExecutionContext) extends PermissionActionFilter {
  val testAccess: String => Future[PermissionAuthorisation] = Permissions.testUser(Permissions.TagAdmin)
  val restrictedAction = "manage sponsorships"
}

case class PillarPermissionsCheck()(implicit val executionContext: ExecutionContext) extends PermissionActionFilter {
  val testAccess: String => Future[PermissionAuthorisation] = Permissions.testUser(Permissions.TagAdmin)
  val restrictedAction = "manage pillars"
}
