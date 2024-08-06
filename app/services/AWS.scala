package services

import java.nio.ByteBuffer

import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsyncClient
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.ec2.model.{DescribeTagsRequest, Filter}
import com.amazonaws.services.kinesis.AmazonKinesisClient
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.sqs.AmazonSQSClient
import com.amazonaws.util.EC2MetadataUtils
import com.twitter.scrooge.ThriftStruct
import play.api.Logging

import scala.collection.JavaConverters._

object AWS {

  lazy val region = Region getRegion Regions.EU_WEST_1

  lazy val EC2Client = region.createClient(classOf[AmazonEC2Client], null, null)
  lazy val CloudWatch = region.createClient(classOf[AmazonCloudWatchAsyncClient], null, null)
  lazy val Kinesis = region.createClient(classOf[AmazonKinesisClient], null, null)
  lazy val S3Client = region.createClient(classOf[AmazonS3Client], null, null)

  private lazy val frontendCredentialsProvider = Config().frontendBucketWriteRole.map(new STSAssumeRoleSessionCredentialsProvider(_, "tagManager"))

  lazy val frontendStaticFilesS3Client = region.createClient(classOf[AmazonS3Client], frontendCredentialsProvider.getOrElse(new ProfileCredentialsProvider("frontend")), null)
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
  lazy val client = AWS.region.createClient(classOf[AmazonDynamoDBClient], null, null)
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
  lazy val SQSClient = AWS.region.createClient(classOf[AmazonSQSClient], null, null)

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
