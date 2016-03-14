package model.jobs

import scala.collection.convert.wrapAll._
import com.google.common.util.concurrent.{ServiceManager, AbstractScheduledService}
import com.google.common.util.concurrent.AbstractScheduledService.Scheduler
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future
import javax.inject._
import org.joda.time.{DateTime, DateTimeZone}
import scala.concurrent.duration._
import play.api.Logger
import repositories._
import services.Dynamo
import scala.util.control.NonFatal
import java.net.InetAddress
import java.util.concurrent.TimeUnit

@Singleton
class JobRunner @Inject() (lifecycle: ApplicationLifecycle) {
  // Scheduler boiler plate
  val serviceManager = new ServiceManager(List(new JobRunnerScheduler(this)))
  lifecycle.addStopHook{ () => Future.successful(stop) }
  serviceManager.startAsync()

  def stop {
    serviceManager.stopAsync()
    serviceManager.awaitStopped(20, TimeUnit.SECONDS)
  }

  // Actual useful code
  val nodeId = InetAddress.getLocalHost().toString() // EC2 machines have a single eth0 so this should work?
  val lockTimeOutMillis = 1000 * 60 * 5

  def tryRun() = {
    try {
      run
    } catch {
      case NonFatal(e) => {
        Logger.error(s"An unexpected exception occurred in the job runner: ${e.getStackTrace}")
      }
    }
  }
  def run() = {
    val currentTime = new DateTime(DateTimeZone.UTC).getMillis
    val lockBreakTime = currentTime - lockTimeOutMillis
    JobRepository.loadAllJobs
      .find(job => isPotentialJob(job, currentTime, lockBreakTime)) // Find first potential job
      .flatMap(JobRepository.lock(_, nodeId, currentTime, lockBreakTime)) // Lock it (will return None if lock fails)
      .foreach(job => { // If we got a job, and locked it then process it
        try {
          job.processStep
        } catch {
          case NonFatal(e) => {
            Logger.error(s"Background job failed on $nodeId, beginning roll back.")
            job.rollback
          }
        } finally {
          JobRepository.upsertJobIfOwned(job, nodeId)
          JobRepository.unlock(job, nodeId)
        }
      })
  }

  private def isPotentialJob(job: Job, currentTime: Long, lockBreakTime: Long): Boolean = {
    // Either waiting job OR the job is locked but it's lock has timed out
    (job.jobStatus == JobStatus.waiting && job.waitUntil < currentTime) || (job.jobStatus == JobStatus.owned && job.lockedAt < lockBreakTime)
  }
}

class JobRunnerScheduler(runner: JobRunner) extends AbstractScheduledService {
  override def runOneIteration(): Unit = runner.tryRun
  override def scheduler(): Scheduler = Scheduler.newFixedDelaySchedule(1, 10, TimeUnit.SECONDS)
}
