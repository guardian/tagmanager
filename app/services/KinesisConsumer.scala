package services

import java.nio.ByteBuffer

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.{IRecordProcessor, IRecordProcessorFactory}
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.{InitialPositionInStream, KinesisClientLibConfiguration, ShutdownReason, Worker}
import com.amazonaws.services.kinesis.clientlibrary.types.{InitializationInput, ProcessRecordsInput, ShutdownInput}
import com.amazonaws.services.kinesis.metrics.interfaces.MetricsLevel
import com.amazonaws.services.kinesis.model.Record
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream
import org.apache.thrift.protocol.{TCompactProtocol, TProtocol}
import org.apache.thrift.transport.TIOStreamTransport
import play.api.Logging

import scala.jdk.CollectionConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions

class KinesisConsumer(streamName: String, appName: String, processor: KinesisStreamRecordProcessor) extends Logging {

  logger.info(s"Creating Kinesis Consumer for (streamName: $streamName, appName: $appName)")

  val kinesisClientLibConfiguration =
    new KinesisClientLibConfiguration(appName, streamName,
      new DefaultAWSCredentialsProviderChain,
      s"$appName-worker")

  kinesisClientLibConfiguration
    .withRegionName(AWS.region.getName)
    .withMetricsLevel(MetricsLevel.NONE)
    .withInitialPositionInStream(InitialPositionInStream.LATEST)

  val worker = new Worker.Builder()
    .recordProcessorFactory(new KinesisProcessorConsumerFactory(appName, processor))
    .config(kinesisClientLibConfiguration)
    .build()

  def start(): Unit = { Future{ worker.run() } }
  def stop(): Unit = { worker.shutdown() }
}

class KinesisProcessorConsumerFactory(appName: String, processor: KinesisStreamRecordProcessor) extends IRecordProcessorFactory {
  override def createProcessor(): IRecordProcessor = new KinesisProcessorConsumer(appName, processor)
}

class KinesisProcessorConsumer(appName: String, processor: KinesisStreamRecordProcessor) extends IRecordProcessor with Logging {


  override def shutdown(shutdownInput: ShutdownInput): Unit = {
    shutdownInput.getShutdownReason match {
      case ShutdownReason.TERMINATE => {
        logger.info(s"terminating $appName consumer")
        //shutdownInput.getCheckpointer.checkpoint
      }
      case _ => logger.info(s"shutting down $appName consumer reason = ${shutdownInput.getShutdownReason}")
    }
  }

  override def initialize(initializationInput: InitializationInput): Unit = {
    logger.info(s"$appName consumer started for shard ${initializationInput.getShardId}")
  }

  override def processRecords(processRecordsInput: ProcessRecordsInput): Unit = {

    processRecordsInput.getRecords.asScala foreach { record =>
      processor.process(record)
    }

  }
}

trait KinesisStreamRecordProcessor {
  def process(record: Record): Unit
}

object KinesisRecordPayloadConversions {

  def kinesisRecordAsThriftCompactProtocol(rec: Record, stripCompressionByte: Boolean = false): TProtocol = {

    val data: ByteBuffer = rec.getData
    val bytes = if (stripCompressionByte) ByteBuffer.wrap(data.array().tail) else data

    val bbis = new ByteBufferBackedInputStream(bytes)
    val transport = new TIOStreamTransport(bbis)
    new TCompactProtocol(transport)
  }
}
