package services

import java.util.concurrent.atomic.AtomicBoolean

import com.amazonaws.services.sqs.model._
import play.api.Logging
import scala.jdk.CollectionConverters._
import scala.annotation.tailrec
import scala.util.control.NonFatal

class SQSQueue(val queueName: String) {

  lazy val queueUrl = {
    val queueNameLookupResponse = SQS.SQSClient.getQueueUrl(new GetQueueUrlRequest(queueName))
    queueNameLookupResponse.getQueueUrl
  }

  def pollMessages(messageCount: Int, waitTimeSeconds: Int) = {
    val response = SQS.SQSClient.receiveMessage(
      new ReceiveMessageRequest(queueUrl).withWaitTimeSeconds(waitTimeSeconds).withMaxNumberOfMessages(messageCount)
    )
    response.getMessages.asScala.toList
  }

  def deleteMessage(message: Message): Unit = {
    SQS.SQSClient.deleteMessage(
      new DeleteMessageRequest(queueUrl, message.getReceiptHandle)
    )
  }

  def postMessage(message: String, delaySeconds: Int = 0): Unit = {
    SQS.SQSClient.sendMessage(
      new SendMessageRequest()
        .withQueueUrl(queueUrl)
        .withMessageBody(message)
        .withDelaySeconds(delaySeconds)
    )
  }
}

trait SQSQueueConsumer extends Logging {
  def queue: SQSQueue
  def processMessage(message: Message): Unit

  val running = new AtomicBoolean(true)

  def stop = running.set(false)

  @tailrec
  final def run(): Unit = {
    if(running.get()) {
      try {
        for(message <- queue.pollMessages(1, 5)) { // only grab one message, do the rest of the cluster gets a chance
          logger.debug(s"processing message form queue ${queue.queueName}")
          processMessage(message)

          queue.deleteMessage(message)
        }
      } catch {
        case NonFatal(e) => {
          logger.error(s"error processing messages from job queue ${queue.queueName}", e)
        }
      }
      run()
    }
  }
}
