package modules

import com.google.inject.AbstractModule
import model.jobs.JobRunner

class JobRunnerModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[JobRunner]).asEagerSingleton()
  }
}
