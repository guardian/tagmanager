package services

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.{IRecordProcessor, IRecordProcessorFactory}
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker
import com.amazonaws.services.kinesis.clientlibrary.types.{ProcessRecordsInput, InitializationInput, ShutdownReason, ShutdownInput}
import com.amazonaws.services.kinesis.metrics.interfaces.MetricsLevel
import com.amazonaws.services.kinesis.model.Record
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream
import org.apache.thrift.protocol.{TCompactProtocol, TProtocol}
import org.apache.thrift.transport.TIOStreamTransport
import play.api.Logger

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class KinesisConsumer(streamName: String, appName: String, processor: KinesisStreamRecordProcessor) {

  val kinesisClientLibConfiguration =
    new KinesisClientLibConfiguration(appName, streamName,
      new DefaultAWSCredentialsProviderChain,
      s"$appName-$streamName-worker");

  kinesisClientLibConfiguration
    .withRegionName(AWS.region.getName)
    .withMetricsLevel(MetricsLevel.NONE)
    .withInitialPositionInStream(InitialPositionInStream.LATEST)

  val worker = new Worker.Builder()
    .recordProcessorFactory(new KinesisProcessorConsumerFactory(appName, processor))
    .config(kinesisClientLibConfiguration)
    .build()

  def start() { Future{ worker.run() } }
  def stop() { worker.shutdown() }

}

class KinesisProcessorConsumerFactory(appName: String, processor: KinesisStreamRecordProcessor) extends IRecordProcessorFactory {
  override def createProcessor(): IRecordProcessor = new KinesisProcessorConsumer(appName, processor)
}

class KinesisProcessorConsumer(appName: String, processor: KinesisStreamRecordProcessor) extends IRecordProcessor {


  override def shutdown(shutdownInput: ShutdownInput): Unit = {
    shutdownInput.getShutdownReason match {
      case ShutdownReason.TERMINATE => {
        Logger.info(s"terminating $appName consumer")
        //shutdownInput.getCheckpointer.checkpoint
      }
      case _ => Logger.info(s"shutting down $appName consumer reason = ${shutdownInput.getShutdownReason}")
    }
  }

  override def initialize(initializationInput: InitializationInput): Unit = {
    Logger.info(s"$appName consumer started for shard ${initializationInput.getShardId}")
  }

  override def processRecords(processRecordsInput: ProcessRecordsInput): Unit = {

    processRecordsInput.getRecords foreach { record =>
      processor.process(record)
    }

    //processRecordsInput.getCheckpointer checkpoint
  }
}

trait KinesisStreamRecordProcessor{

  def payload(record: Record) = new String(record.getData.array(), "UTF-8")

  def process(record: Record): Unit

}

object KinesisRecordPayloadConversions {
  implicit def kinesisRecordAsThriftCompactProtocol(rec: Record): TProtocol = {

    val data = rec.getData

    val settings = data.get() //compression bit

    val bbis = new ByteBufferBackedInputStream(data)
    val transport = new TIOStreamTransport(bbis)
    new TCompactProtocol(transport)
  }
}
