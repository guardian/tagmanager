package modules.clustersync

import com.google.inject.AbstractModule

class ClusterSyncModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[ClusterSynchronisation]).asEagerSingleton()
  }
}
