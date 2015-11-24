package modules.jobrunner

import java.util.concurrent.Executors
import javax.inject.{Inject, Singleton}

import com.google.inject.AbstractModule
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import repositories.ContentAPI
import services.SQS
import scala.concurrent.{ExecutionContext, Future}

class JobRunnerModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[JobRunnerServices]).asEagerSingleton()
  }
}

@Singleton
class JobRunnerServices @Inject() (lifecycle: ApplicationLifecycle) {

  Logger.info("Starting job runner services")
  private val executorService = Executors.newFixedThreadPool(25)
  private implicit val executionContext = ExecutionContext.fromExecutor(executorService)

  lifecycle.addStopHook{ () => Future.successful(stop) }

  val jobQueueConsumer = new JobQueueConsumer(SQS.jobQueue)
  Future( jobQueueConsumer.run )

  def stop: Unit = {
    Logger.info("stopping job queue consumer")
    jobQueueConsumer.stop

    Logger.info("stopping content api internals")
    ContentAPI.shutdown

    Logger.info("stopping JobRunnerServices execution context")
    executorService.shutdown
  }

}