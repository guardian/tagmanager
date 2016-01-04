package permissions

import com.gu.editorial.permissions.client._
import services.Config
import scala.concurrent.{Future, ExecutionContext}

object Permissions extends PermissionsProvider {

  lazy val BatchTag = Permission("batch_tag", "tag-manager", PermissionDenied)

  lazy val all = Seq(BatchTag)

  implicit def config = PermissionsConfig(
    app = "tag-manager",
    all = all,
    s3BucketPrefix = Config().permissionsStage,
    s3Region = Some("eu-west-1")
  )

  def testUser(permission: Permission)(email: String): Future[PermissionAuthorisation] = {
    println("Permissions for: " + email)
    implicit val permissionsUser: PermissionsUser = PermissionsUser(email)

    Permissions.get(permission)
  }

  def getPermissionsForUser(email: String): Future[Map[String, Boolean]] = {
    implicit val permissionsUser: PermissionsUser = PermissionsUser(email)

    Permissions.list.map(_.filter(_._1.app == "tag-manager").flatMap( _ match {
      case (p: Permission, PermissionGranted) => Map(p.name -> true)
      case (p: Permission, PermissionDenied) => Map(p.name -> false)
    }))
  }
}


