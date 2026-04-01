package services

import java.nio.ByteBuffer
import java.util.UUID

import software.amazon.kinesis.common.ConfigsBuilder
import software.amazon.kinesis.coordinator.Scheduler
import software.amazon.kinesis.lifecycle.events._
import software.amazon.kinesis.processor.{ShardRecordProcessor, ShardRecordProcessorFactory}
import software.amazon.kinesis.retrieval.KinesisClientRecord
import software.amazon.kinesis.retrieval.polling.PollingConfig
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream
import org.apache.thrift.protocol.{TCompactProtocol, TProtocol}
import org.apache.thrift.transport.TIOStreamTransport
import play.api.Logging

import scala.jdk.CollectionConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class KinesisConsumer(streamName: String, appName: String, processor: KinesisStreamRecordProcessor) extends Logging {

  logger.info(s"Creating Kinesis Consumer for (streamName: $streamName, appName: $appName)")

  private val configsBuilder = new ConfigsBuilder(
    streamName,
    appName,
    AWS.kinesisAsyncClient,
    AWS.dynamoDbAsyncClient,
    AWS.cloudWatchAsyncClient,
    s"$appName-worker-${UUID.randomUUID()}",
    new KinesisProcessorConsumerFactory(appName, processor)
  )

  private val retrievalConfig = configsBuilder.retrievalConfig()
    .retrievalSpecificConfig(new PollingConfig(streamName, AWS.kinesisAsyncClient))

  private val scheduler = new Scheduler(
    configsBuilder.checkpointConfig(),
    configsBuilder.coordinatorConfig(),
    configsBuilder.leaseManagementConfig(),
    configsBuilder.lifecycleConfig(),
    configsBuilder.metricsConfig(),
    configsBuilder.processorConfig(),
    retrievalConfig
  )

  def start(): Unit = { Future { scheduler.run() } }
  def stop(): Unit = {
    Future { scheduler.shutdown() }
  }
}

class KinesisProcessorConsumerFactory(appName: String, processor: KinesisStreamRecordProcessor) extends ShardRecordProcessorFactory {
  override def shardRecordProcessor(): ShardRecordProcessor = new KinesisProcessorConsumer(appName, processor)
}

class KinesisProcessorConsumer(appName: String, processor: KinesisStreamRecordProcessor) extends ShardRecordProcessor with Logging {

  override def initialize(initializationInput: InitializationInput): Unit = {
    logger.info(s"$appName consumer started for shard ${initializationInput.shardId()}")
  }

  override def processRecords(processRecordsInput: ProcessRecordsInput): Unit = {
    processRecordsInput.records().asScala.foreach { record =>
      processor.process(record)
    }
  }

  override def leaseLost(leaseLostInput: LeaseLostInput): Unit = {
    logger.info(s"$appName consumer lease lost")
  }

  override def shardEnded(shardEndedInput: ShardEndedInput): Unit = {
    logger.info(s"$appName consumer shard ended")
    try {
      shardEndedInput.checkpointer().checkpoint()
    } catch {
      case e: Exception => logger.error(s"Error checkpointing at shard end", e)
    }
  }

  override def shutdownRequested(shutdownRequestedInput: ShutdownRequestedInput): Unit = {
    logger.info(s"$appName consumer shutdown requested")
    try {
      shutdownRequestedInput.checkpointer().checkpoint()
    } catch {
      case e: Exception => logger.error(s"Error checkpointing at shutdown", e)
    }
  }
}

trait KinesisStreamRecordProcessor {
  def process(record: KinesisClientRecord): Unit
}

object KinesisRecordPayloadConversions {

  def kinesisRecordAsThriftCompactProtocol(rec: KinesisClientRecord, stripCompressionByte: Boolean = false): TProtocol = {

    val data: ByteBuffer = rec.data()
    val bytes = if (stripCompressionByte) {
      val remaining = new Array[Byte](data.remaining())
      data.duplicate().get(remaining)
      ByteBuffer.wrap(remaining.tail)
    } else data

    val bbis = new ByteBufferBackedInputStream(bytes)
    val transport = new TIOStreamTransport(bbis)
    new TCompactProtocol(transport)
  }
}
