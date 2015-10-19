package services

import com.gu.logback.appender.kinesis.KinesisAppender
import org.slf4j.{Logger => SLFLogger, LoggerFactory}
import ch.qos.logback.classic.{Logger => LogbackLogger}
import net.logstash.logback.layout.LogstashLayout
import play.api.Logger


object LogShipping extends AwsInstanceTags {

  val rootLogger = LoggerFactory.getLogger(SLFLogger.ROOT_LOGGER_NAME).asInstanceOf[LogbackLogger]

  def bootstrap {
    rootLogger.info("bootstrapping kinesis appender if configured correctly")
    for (
      stack <- readTag("Stack");
      app <- readTag("App");
      stage <- readTag("Stage");
      streamName <- Config().logShippingStreamName
    ) {

      Logger.info(s"bootstrapping kinesis appender with $stack -> $app -> $stage")
      val context = rootLogger.getLoggerContext

      val layout = new LogstashLayout()
      layout.setContext(context)
      layout.setCustomFields(s"""{"stack":"$stack","app":"$app","stage":"$stage"}""")
      layout.start()

      val appender = new KinesisAppender()
      appender.setBufferSize(1000)
      appender.setEndpoint(AWS.region.getName)
      appender.setStreamName(streamName)
      appender.setContext(context)
      appender.setLayout(layout)

      appender.start()

      rootLogger.addAppender(appender)
      rootLogger.info("Configured kinesis appender")
    }
  }
}
