package controllers

import com.gu.pandahmac.HMACAuthActions
import com.gu.pandomainauth.PanDomain
import com.gu.pandomainauth.model.AuthenticatedUser
import services.Config
import permissions.Permissions
import play.api.Logging

trait PanDomainAuthActions extends HMACAuthActions with Logging {

  override def validateUser(authedUser: AuthenticatedUser): Boolean = {
    val isValid = PanDomain.guardianValidation(authedUser)

    val canAccess = Permissions.testUser(Permissions.TagManagerAccess)(authedUser.user.email)

    if (!isValid) {
      logger.warn(s"User ${authedUser.user.email} is not valid")
    } else if (!canAccess) {
      logger.warn(s"User ${authedUser.user.email} does not have tag_manager_access permission")
    }

    isValid // TODO && canAccess
  }

  override def cacheValidation = true

  override def authCallbackUrl: String = Config().pandaAuthCallback

  override lazy val secret: String = Config().hmacSecret
}
