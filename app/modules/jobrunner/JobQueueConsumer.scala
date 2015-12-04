package modules.jobrunner

import com.amazonaws.services.sqs.model.Message
import model.jobs.JobRunner
import play.api.Logger
import repositories.JobRepository
import services.{SQSQueueConsumer, SQSQueue}

import scala.concurrent.{Future, ExecutionContext}

class JobQueueConsumer(override val queue: SQSQueue)(implicit executionContext: ExecutionContext) extends SQSQueueConsumer {

  override def processMessage(message: Message): Unit = {
    val jobId = message.getBody.toLong

    JobRepository.getJob(jobId) foreach { job => Future {
      val updatedJob = JobRunner.run(job)
      updatedJob foreach{ j =>
        Logger.info(s"job $j not completed, queueing next run in 15s")
        queue.postMessage(j.id.toString, delaySeconds = 15)
      }
    }}
  }
}
