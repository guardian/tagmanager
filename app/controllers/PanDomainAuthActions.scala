package controllers

import com.gu.pandahmac.HMACAuthActions
import com.gu.pandomainauth.model.AuthenticatedUser
import services.Config

trait PanDomainAuthActions extends HMACAuthActions {

  override def validateUser(authedUser: AuthenticatedUser): Boolean = {
    (authedUser.user.email endsWith ("@guardian.co.uk")) && authedUser.multiFactor
  }

  override def cacheValidation = true

  override def authCallbackUrl: String = Config().pandaAuthCallback

  override lazy val domain: String = Config().pandaDomain

  override lazy val system: String = "tagmanager"

  override lazy val secret: String = Config().hmacSecret
}
