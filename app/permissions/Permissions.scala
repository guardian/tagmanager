package permissions

import com.amazonaws.auth.{AWSCredentialsProvider, DefaultAWSCredentialsProviderChain, STSAssumeRoleSessionCredentialsProvider}
import com.gu.permissions.{PermissionDefinition, PermissionsConfig, PermissionsProvider}
import services.Config

//import scala.concurrent.Future

//object Permissions extends PermissionsProvider {

//  lazy val TagEdit = Permission("tag_edit", "tag-manager", PermissionDenied)
//  lazy val TagAdmin = Permission("tag_admin", "tag-manager", PermissionDenied)
//  lazy val CommercialTags = Permission("commercial_tags", "tag-manager", PermissionDenied)
//  lazy val TagUnaccessible = Permission("tag_no_one", "tag-manager", PermissionDenied)
//
//  lazy val all = Seq(TagAdmin)

//  implicit def config = PermissionsConfig(
//    app = "tag-manager",
//    all = all,
//    s3BucketPrefix = Config().permissionsStage,
//    s3Region = Some("eu-west-1")
//  )

//  def testUser(permission: Permission)(email: String): Future[PermissionAuthorisation] = {
//    println("Permissions for: " + email)
//    implicit val permissionsUser: PermissionsUser = PermissionsUser(email)
//
//    Permissions.get(permission)
//  }
//
//  def getPermissionsForUser(email: String): Future[Map[String, Boolean]] = {
//    implicit val permissionsUser: PermissionsUser = PermissionsUser(email)
//
//    Permissions.list.map(_.filter(_._1.app == "tag-manager").flatMap( _ match {
//      case (p: Permission, PermissionGranted) => Map(p.name -> true)
//      case (p: Permission, PermissionDenied) => Map(p.name -> false)
//    }))
//  }
object Permissions {
  val app = "tag-manager"

  val TagEdit = PermissionDefinition("tag_edit", app)
  val TagAdmin = PermissionDefinition("tag_admin", app)
  val CommercialTags = PermissionDefinition("commercial_tags", app)
  val TagUnaccessible = PermissionDefinition("tag_no_one", app)

  private val permissionDefinitions = Map(
    "tagEdit" -> TagEdit,
    "tagAdmin" -> TagAdmin,
    "commercialTags" -> CommercialTags,
    "tagUnaccessible" -> TagUnaccessible
  )

  private val credentials: AWSCredentialsProvider = new DefaultAWSCredentialsProviderChain()

  private val permissions: PermissionsProvider = PermissionsProvider(PermissionsConfig(Config().permissionsStage, Config().aws.region, credentials))

  def testUser(email: String)(permission: PermissionDefinition): Boolean = {
    println("Permissions for: " + email)
    permissions.hasPermission(permission, email)
  }
  def getPermissionsForUser(email: String): Map[String, Boolean] = permissionDefinitions.transform((_, permission) => permissions.hasPermission(permission, email))
}

