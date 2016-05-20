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
    val lockBreakTime = currentTime - JobRunner.lockTimeOutMillis
    JobRepository.loadAllJobs
      .filter(validJobs)
      .find(job => isPotentialJob(job, currentTime, lockBreakTime)) // Find first potential job
      .flatMap(JobRepository.lock(_, JobRunner.nodeId, currentTime, lockBreakTime)) // Lock it (will return None if lock fails)
      .foreach(job => { // If we got a job, and locked it then process it
        try {
          job.process
        } catch {
          case NonFatal(e) => {
            // This catch exists to prevent an unexpected failure knocking over the entire job runner
            Logger.error(s"Background job failed on ${JobRunner.nodeId}.")
          }
        } finally {
          job.checkIfComplete
          JobRepository.upsertJobIfOwned(job, JobRunner.nodeId)
          JobRepository.unlock(job, JobRunner.nodeId)
        }
      })
  }

  private def isPotentialJob(job: Job, currentTime: Long, lockBreakTime: Long): Boolean = {
    // Either waiting job OR the job is locked but it's lock has timed out
    (job.jobStatus == JobStatus.waiting && job.waitUntil < currentTime) || (job.jobStatus == JobStatus.owned && job.lockedAt < lockBreakTime)
  }

  private def validJobs(job: Job): Boolean = {
    if (job.jobStatus == JobStatus.complete
      || job.jobStatus ==  JobStatus.failed
      || job.jobStatus == JobStatus.rolledback) {
        if (job.createdAt < new DateTime(DateTimeZone.UTC).getMillis - JobRunner.cleanUpMillis) {
          Logger.info("Cleaning up old job: " + job.id)
          JobRepository.deleteIfTerminal(job.id)
        }
        return false;
    }
  return true;
  }
}

object JobRunner {
  val nodeId = InetAddress.getLocalHost().toString() // EC2 machines have a single eth0 so this should work?
  val lockTimeOutMillis = 1000 * 60 * 5
  val cleanUpMillis = 1000 * 60 * 60 * 24
}

class JobRunnerScheduler(runner: JobRunner) extends AbstractScheduledService {
  override def runOneIteration(): Unit = runner.tryRun
  override def scheduler(): Scheduler = Scheduler.newFixedDelaySchedule(1, 10, TimeUnit.SECONDS)
}
