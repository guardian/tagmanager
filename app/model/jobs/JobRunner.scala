package model.jobs

import play.api.inject.ApplicationLifecycle
import javax.inject._
import org.joda.time.{DateTime, DateTimeZone}
import scala.concurrent.duration._
import play.api.Logger
import repositories._
import services.Dynamo
import scala.util.control.NonFatal

@Singleton
class JobRunner @Inject() (lifecycle: ApplicationLifecycle) {
  val nodeId: String = "foo" // TODO set

  def run() = {
    val allJobs = JobRepository.loadAllJobs
    val currentTime = new DateTime(DateTimeZone.UTC).getMillis

    // Contention on table head?
    allJobs.foreach { job =>
      if (job.status == JobStatus.waiting && job.waitUntil < currentTime) {
        if (JobRepository.lock(job, nodeId)) {
          try {
            job.processStep
          } catch {
            case NonFatal(e) => {
              Logger.error(s"Background job failed on $nodeId, beginning roll back.")
              job.rollback
            }
          } finally {
            JobRepository.updateJobIfOwned(job, nodeId)
            JobRepository.unlock(job)
          }
        }
      }
    }

    Thread.sleep(1000)
  }
}
