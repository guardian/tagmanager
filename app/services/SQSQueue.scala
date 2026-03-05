package services

import java.util.concurrent.atomic.AtomicBoolean

import software.amazon.awssdk.services.sqs.model._
import play.api.Logging
import scala.jdk.CollectionConverters._
import scala.annotation.tailrec
import scala.util.control.NonFatal

class SQSQueue(val queueName: String) {

  lazy val queueUrl = {
    val queueNameLookupResponse = SQS.SQSClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build())
    queueNameLookupResponse.queueUrl()
  }

  def pollMessages(messageCount: Int, waitTimeSeconds: Int) = {
    val response = SQS.SQSClient.receiveMessage(
      ReceiveMessageRequest.builder()
        .queueUrl(queueUrl)
        .waitTimeSeconds(waitTimeSeconds)
        .maxNumberOfMessages(messageCount)
        .build()
    )
    response.messages().asScala.toList
  }

  def deleteMessage(message: Message): Unit = {
    SQS.SQSClient.deleteMessage(
      DeleteMessageRequest.builder()
        .queueUrl(queueUrl)
        .receiptHandle(message.receiptHandle())
        .build()
    )
  }

  def postMessage(message: String, delaySeconds: Int = 0): Unit = {
    SQS.SQSClient.sendMessage(
      SendMessageRequest.builder()
        .queueUrl(queueUrl)
        .messageBody(message)
        .delaySeconds(delaySeconds)
        .build()
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
