package controllers

import com.gu.pandahmac.HMACAuthActions
import com.gu.pandomainauth.PanDomain
import com.gu.pandomainauth.model.AuthenticatedUser
import services.Config
import permissions.Permissions
import play.api.Logging
import play.api.mvc.{RequestHeader, Result}
import play.api.mvc.Results.Forbidden

trait PanDomainAuthActions extends HMACAuthActions with Logging {

  private def noPermissionMessage(authedUser: AuthenticatedUser): String =
    s"user ${authedUser.user.email} does not have ${Permissions.TagManagerAccess.name} permission"

  override def validateUser(authedUser: AuthenticatedUser): Boolean = {
    val isValid = PanDomain.guardianValidation(authedUser)

    val canAccess = Permissions.testUser(Permissions.TagManagerAccess)(authedUser.user.email)

    if (!isValid) {
      logger.warn(s"User ${authedUser.user.email} is not valid")
    } else if (!canAccess) {
      logger.warn(noPermissionMessage(authedUser))
    }

    isValid && canAccess
  }

  override def showUnauthedMessage(message: String)(implicit request: RequestHeader): Result =
    Forbidden(views.html.Application.authError(message))

  override def invalidUserMessage(claimedAuth: AuthenticatedUser): String = {
    val hasAccess = Permissions.testUser(Permissions.TagManagerAccess)(claimedAuth.user.email)

    if (!hasAccess) noPermissionMessage(claimedAuth)
    else super.invalidUserMessage(claimedAuth)
  }

  override def cacheValidation = true

  override def authCallbackUrl: String = Config().pandaAuthCallback

  override lazy val secret: String = Config().hmacSecret
}
