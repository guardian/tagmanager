package modules

import ch.qos.logback.classic.{Logger => LogbackLogger}
import com.google.inject.AbstractModule
import com.gu.logback.appender.kinesis.KinesisAppender
import net.logstash.logback.layout.LogstashLayout
import org.slf4j.{Logger => SLFLogger, LoggerFactory}
import play.api.Logger
import services.{AWS, AwsInstanceTags, Config}


class LogShipping extends AbstractModule with AwsInstanceTags {

  val rootLogger = LoggerFactory.getLogger(SLFLogger.ROOT_LOGGER_NAME).asInstanceOf[LogbackLogger]

  def configure {
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
      appender.setRegion(AWS.region.getName)
      appender.setStreamName(streamName)
      appender.setContext(context)
      appender.setLayout(layout)

      appender.start()

      rootLogger.addAppender(appender)
      rootLogger.info("Configured kinesis appender")
    }
  }
}
