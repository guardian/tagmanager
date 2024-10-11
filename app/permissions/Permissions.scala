package permissions

import com.amazonaws.auth.{AWSCredentialsProvider, DefaultAWSCredentialsProviderChain}
import com.gu.permissions.{PermissionDefinition, PermissionsConfig, PermissionsProvider}
import services.Config
import com.madgag.scala.collection.decorators._

object Permissions {
  val app = "tag-manager"

  val TagManagerAccess: PermissionDefinition = PermissionDefinition("tag_manager_access", app)
  val TagEdit: PermissionDefinition = PermissionDefinition("tag_edit", app)
  val TagAdmin: PermissionDefinition = PermissionDefinition("tag_admin", app)
  val CommercialTags: PermissionDefinition = PermissionDefinition("commercial_tags", app)

  private val permissionDefinitions = Map(
    "tag_manager_access" -> TagManagerAccess,
    "tag_edit" -> TagEdit,
    "tag_admin" -> TagAdmin,
    "commercial_tags" -> CommercialTags,
  )

  private val credentials: AWSCredentialsProvider = new DefaultAWSCredentialsProviderChain()

  private val permissions: PermissionsProvider = PermissionsProvider(PermissionsConfig(Config().permissionsStage, Config().aws.region, credentials))

  def testUser(permission: PermissionDefinition)(email: String): Boolean = {
    println("Permissions for: " + email)
    permissions.hasPermission(permission, email)
  }
  def getPermissionsForUser(email: String): Map[String, Boolean] =
    permissionDefinitions.mapV(permission => permissions.hasPermission(permission, email))
}

