package modules

import com.google.inject.AbstractModule

import play.api.inject.ApplicationLifecycle
import javax.inject._

import com.gu.editorial.permissions.client._
import permissions._

// This is requried because there's an issue in the permissions client
// which causes the permissions to come back as denied for the first person
// to request them. Seems to be a timing bug...
//
// This should only be a temporary fix @ 2016/02/09

@Singleton
class PermissionsLoader @Inject() (lifecycle: ApplicationLifecycle) {
  //load

  def load {
    Permissions.list(PermissionsUser("preload@permissions"))
  }
}

class PermissionsLoadModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[PermissionsLoader]).asEagerSingleton()
  }
}
