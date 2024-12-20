package services

import java.nio.ByteBuffer
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsyncClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder
import com.amazonaws.services.ec2.model.{DescribeTagsRequest, Filter}
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder
import com.amazonaws.services.s3.{AmazonS3Client, AmazonS3ClientBuilder}
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.amazonaws.util.EC2MetadataUtils
import com.twitter.scrooge.ThriftStruct
import play.api.Logging
import services.AWS.region

import scala.jdk.CollectionConverters._

object AWS {

  lazy val region = Region getRegion Regions.EU_WEST_1

  lazy val EC2Client = AmazonEC2ClientBuilder
    .standard()
    .withRegion(region.getName)
    .build()
  lazy val CloudWatch = AmazonCloudWatchAsyncClientBuilder
    .standard()
    .withRegion(region.getName)
    .build()
  lazy val Kinesis = AmazonKinesisClientBuilder
    .standard()
    .withRegion(region.getName)
    .build()
  lazy val S3Client = AmazonS3ClientBuilder
    .standard()
    .withRegion(region.getName)
    .build()

  private lazy val frontendCredentialsProvider = Config().frontendBucketWriteRole.map(
    new STSAssumeRoleSessionCredentialsProvider.Builder(_, "tagManager").build()
  )

  lazy val frontendStaticFilesS3Client = AmazonS3ClientBuilder
    .standard()
    .withCredentials(frontendCredentialsProvider.getOrElse(new ProfileCredentialsProvider("frontend")))
    .withRegion(region.getName)
    .build()
}

trait AwsInstanceTags {
  lazy val instanceId = Option(EC2MetadataUtils.getInstanceId)

  def readTag(tagName: String) = {
    instanceId.flatMap { id =>
      val tagsResult = AWS.EC2Client.describeTags(
        new DescribeTagsRequest().withFilters(
          new Filter("resource-type").withValues("instance"),
          new Filter("resource-id").withValues(id),
          new Filter("key").withValues(tagName)
        )
      )
      tagsResult.getTags.asScala.find(_.getKey == tagName).map(_.getValue)
    }
  }
}

object Dynamo {
  lazy val client = AmazonDynamoDBClientBuilder
    .standard()
    .withRegion(AWS.region.getName)
    .build()
  lazy val dynamoDb = new DynamoDB(client)

  lazy val tagTable = dynamoDb.getTable(Config().tagsTableName)
  lazy val sectionTable = dynamoDb.getTable(Config().sectionsTableName)
  lazy val sponsorshipTable = dynamoDb.getTable(Config().sponsorshipTableName)
  lazy val sequenceTable = dynamoDb.getTable(Config().sequenceTableName)
  lazy val jobTable = dynamoDb.getTable(Config().jobTableName)
  lazy val tagAuditTable = dynamoDb.getTable(Config().tagAuditTableName)
  lazy val sectionAuditTable = dynamoDb.getTable(Config().sectionAuditTableName)
  lazy val appAuditTable = dynamoDb.getTable(Config().appAuditTableName)
  lazy val pillarTable = dynamoDb.getTable(Config().pillarsTableName)
  lazy val pillarAuditTable = dynamoDb.getTable(Config().pillarsAuditTableName)

  lazy val reindexProgressTable = dynamoDb.getTable(Config().reindexProgressTableName)

  lazy val clusterStatusTable = dynamoDb.getTable(Config().clusterStatusTableName)
  lazy val referencesTypeTable = dynamoDb.getTable(Config().referencesTypeTableName)
}

object SQS {
  lazy val SQSClient = AmazonSQSClientBuilder
    .standard()
    .withRegion(region.getName)
    .build()

  lazy val jobQueue = new SQSQueue(Config().jobQueueName)
}

class KinesisStreamProducer(streamName: String, requireCompressionByte: Boolean = false) extends Logging {

  def publishUpdate(key: String, data: String): Unit = {
    publishUpdate(key, ByteBuffer.wrap(data.getBytes("UTF-8")))
  }

  def publishUpdate(key: String, data: Array[Byte]): Unit = {
    publishUpdate(key, ByteBuffer.wrap(data))
  }

  def publishUpdate(key: String, struct: ThriftStruct): Unit = {
    logger.info(s"Kinesis Producer publishUpdate for streamName: $streamName")
    val thriftKinesisEvent: Array[Byte] = ThriftSerializer.serializeToBytes(struct, requireCompressionByte)
    publishUpdate(key, ByteBuffer.wrap(thriftKinesisEvent))
  }

  def publishUpdate(key: String, dataBuffer: ByteBuffer): Unit = {
    AWS.Kinesis.putRecord(streamName, dataBuffer, key)
  }
}

object KinesisStreams {
  lazy val tagUpdateStream = new KinesisStreamProducer(streamName = Config().tagUpdateStreamName, requireCompressionByte = true)
  lazy val sectionUpdateStream = new KinesisStreamProducer(streamName = Config().sectionUpdateStreamName, requireCompressionByte = true)
  lazy val reindexTagsStream = new KinesisStreamProducer(streamName = Config().reindexTagsStreamName, requireCompressionByte = true)
  lazy val reindexSectionsStream = new KinesisStreamProducer(streamName = Config().reindexSectionsStreamName, requireCompressionByte = true)
  lazy val taggingOperationsStream = new KinesisStreamProducer(streamName = Config().taggingOperationsStreamName)
  lazy val taggingOperationsReIndexStream = new KinesisStreamProducer(streamName = Config().taggingOperationsReIndexStreamName)
  lazy val commercialExpiryStream = new KinesisStreamProducer(streamName = Config().commercialExpiryStreamName)
  lazy val pillarUpdateStream = new KinesisStreamProducer(streamName = Config().pillarUpdateStreamName, requireCompressionByte = true)
  lazy val reindexPillarsStream = new KinesisStreamProducer(streamName = Config().reindexPillarsStreamName, requireCompressionByte = true)
}
