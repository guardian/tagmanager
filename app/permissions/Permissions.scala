package permissions

import com.amazonaws.auth.{AWSCredentialsProvider, DefaultAWSCredentialsProviderChain}
import com.gu.permissions.{PermissionDefinition, PermissionsConfig, PermissionsProvider}
import services.Config

object Permissions {
  val app = "tag-manager"

  val TagEdit: PermissionDefinition = PermissionDefinition("tag_edit", app)
  val TagAdmin: PermissionDefinition = PermissionDefinition("tag_admin", app)
  val CommercialTags: PermissionDefinition = PermissionDefinition("commercial_tags", app)
  val TagUnaccessible: PermissionDefinition = PermissionDefinition("tag_no_one", app)

  private val permissionDefinitions = Map(
    "tag_edit" -> TagEdit,
    "tag_admin" -> TagAdmin,
    "commercial_tags" -> CommercialTags,
    "tag_no_one" -> TagUnaccessible
  )

  private val credentials: AWSCredentialsProvider = new DefaultAWSCredentialsProviderChain()

  private val permissions: PermissionsProvider = PermissionsProvider(PermissionsConfig(Config().permissionsStage, Config().aws.region, credentials))

  def testUser(permission:PermissionDefinition)(email: String): Boolean = {
    println("Permissions for: " + email)
    permissions.hasPermission(permission, email)
  }
  def getPermissionsForUser(email: String): Map[String, Boolean] = permissionDefinitions.transform((_, permission) => permissions.hasPermission(permission, email))
}

