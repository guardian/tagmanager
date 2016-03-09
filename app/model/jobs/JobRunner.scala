package model.jobs

import play.api.inject.ApplicationLifecycle
import javax.inject._
import org.joda.time.{DateTime, DateTimeZone}
import scala.concurrent.duration._
import play.api.Logger
import repositories._
import services.Dynamo
import scala.util.control.NonFatal
import java.net.InetAddress

@Singleton
class JobRunner @Inject() (lifecycle: ApplicationLifecycle) {
  val nodeId = InetAddress.getLocalHost().toString() // EC2 machines have a single eth0 so this should work?
  val lockTimeOutMillis = 1000 * 60 * 5

  def run() = {
    val currentTime = new DateTime(DateTimeZone.UTC).getMillis
    JobRepository.loadAllJobs
      .find(job => isPotentialJob(job, currentTime)) // Find first potential job
      .flatMap(JobRepository.lock(_, nodeId)) // Lock it (will return None if lock fails)
      .foreach(job => { // If we got a job, and locked it then process it
        try {
          job.processStep
        } catch {
          case NonFatal(e) => {
            Logger.error(s"Background job failed on $nodeId, beginning roll back.")
            job.rollback
          }
        } finally {
          JobRepository.updateJobIfOwned(job, nodeId)
          JobRepository.unlock(job, nodeId)
        }
      })

    Thread.sleep(3000)
  }

  private def isPotentialJob(job: Job, currentTime: Long): Boolean = {
    // Either waiting job OR the job is locked but it's lock has timed out
    (job.status == JobStatus.waiting && job.waitUntil < currentTime) || (job.status == JobStatus.owned && job.lockedAt < currentTime - lockTimeOutMillis)
  }
}
