package services

import java.nio.ByteBuffer

// AWS SDK 1 - kept for DynamoDB, Kinesis, CloudWatch (to be migrated in future PRs)
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsyncClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder
import com.amazonaws.util.EC2MetadataUtils

// AWS SDK 2 - migrated services
import software.amazon.awssdk.auth.credentials.{ProfileCredentialsProvider => SdkV2ProfileCredentialsProvider}
import software.amazon.awssdk.regions.{Region => SdkV2Region}
import software.amazon.awssdk.services.ec2.Ec2Client
import software.amazon.awssdk.services.ec2.model.{DescribeTagsRequest, Filter}
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sts.StsClient
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider

import com.twitter.scrooge.ThriftStruct
import play.api.Logging

import scala.jdk.CollectionConverters._

object AWS {

  // SDK 1 region
  lazy val region = Region getRegion Regions.EU_WEST_1

  // SDK 2 region
  lazy val regionV2 = SdkV2Region.EU_WEST_1

  // SDK 2 EC2 client
  lazy val EC2Client = Ec2Client.builder()
    .region(regionV2)
    .build()

  // SDK 1 clients
  lazy val CloudWatch = AmazonCloudWatchAsyncClientBuilder
    .standard()
    .withRegion(region.getName)
    .build()
  lazy val Kinesis = AmazonKinesisClientBuilder
    .standard()
    .withRegion(region.getName)
    .build()

  // SDK 2 S3 client
  lazy val s3Client: S3Client = S3Client.builder()
    .region(regionV2)
    .build()

  private lazy val frontendCredentialsProvider = Config().frontendBucketWriteRole.map { role =>
    StsAssumeRoleCredentialsProvider.builder()
      .stsClient(StsClient.builder().region(regionV2).build())
      .refreshRequest(AssumeRoleRequest.builder()
        .roleArn(role)
        .roleSessionName("tagManager")
        .build())
      .build()
  }

  lazy val frontendStaticFilesS3Client = S3Client.builder()
    .credentialsProvider(frontendCredentialsProvider.getOrElse(SdkV2ProfileCredentialsProvider.create("frontend")))
    .region(regionV2)
    .build()
}

trait AwsInstanceTags {
  lazy val instanceId = Option(EC2MetadataUtils.getInstanceId)

  def readTag(tagName: String) = {
    instanceId.flatMap { id =>
      val tagsResult = AWS.EC2Client.describeTags(
        DescribeTagsRequest.builder()
          .filters(
            Filter.builder().name("resource-type").values("instance").build(),
            Filter.builder().name("resource-id").values(id).build(),
            Filter.builder().name("key").values(tagName).build()
          )
          .build()
      )
      tagsResult.tags().asScala.find(_.key() == tagName).map(_.value())
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
  lazy val SQSClient = SqsClient.builder()
    .region(AWS.regionV2)
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
