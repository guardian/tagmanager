package services

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider

import java.nio.ByteBuffer
import software.amazon.awssdk.imds.Ec2MetadataClient
import software.amazon.awssdk.auth.credentials.{ProfileCredentialsProvider => SdkV2ProfileCredentialsProvider}
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.regions.{Region => SdkV2Region}
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.ec2.Ec2Client
import software.amazon.awssdk.services.ec2.model.{DescribeTagsRequest, Filter}
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.kinesis.KinesisClient
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sts.StsClient
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider

import com.twitter.scrooge.ThriftStruct
import play.api.Logging

import scala.jdk.CollectionConverters._

object AWS {

  lazy val regionV2 = SdkV2Region.EU_WEST_1

  lazy val EC2Client = Ec2Client.builder()
    .region(regionV2)
    .build()

  lazy val Kinesis = KinesisClient.builder()
    .region(regionV2)
    .build()

  // Async clients for Kinesis Consumer Library
  lazy val kinesisAsyncClient: KinesisAsyncClient = KinesisAsyncClient.builder()
    .region(regionV2)
    .build()

  lazy val dynamoDbAsyncClient: DynamoDbAsyncClient = DynamoDbAsyncClient.builder()
    .region(regionV2)
    .build()

  lazy val cloudWatchAsyncClient: CloudWatchAsyncClient = CloudWatchAsyncClient.builder()
    .region(regionV2)
    .build()

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

  lazy val credentialsProvider = DefaultCredentialsProvider
    .builder()
    .profileName("composer")
    .build()
}

trait AwsInstanceTags {
  private lazy val metadataClient = Ec2MetadataClient.create()

  lazy val instanceId: Option[String] = scala.util.Try(
    metadataClient.get("/latest/meta-data/instance-id").asString()
  ).toOption.filter(_.nonEmpty)

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
  lazy val client: DynamoDbClient = DynamoDbClient.builder()
    .region(AWS.regionV2)
    .build()

  lazy val tagTable = DynamoTable.create(client, Config().tagsTableName)
  lazy val sectionTable = DynamoTable.create(client, Config().sectionsTableName)
  lazy val sponsorshipTable = DynamoTable.create(client, Config().sponsorshipTableName)
  lazy val sequenceTable = DynamoTable.create(client, Config().sequenceTableName)
  lazy val jobTable = DynamoTable.create(client, Config().jobTableName)
  lazy val tagAuditTable = DynamoTable.create(client, Config().tagAuditTableName)
  lazy val sectionAuditTable = DynamoTable.create(client, Config().sectionAuditTableName)
  lazy val appAuditTable = DynamoTable.create(client, Config().appAuditTableName)
  lazy val pillarTable = DynamoTable.create(client, Config().pillarsTableName)
  lazy val pillarAuditTable = DynamoTable.create(client, Config().pillarsAuditTableName)
  lazy val reindexProgressTable = DynamoTable.create(client, Config().reindexProgressTableName)
  lazy val clusterStatusTable = DynamoTable.create(client, Config().clusterStatusTableName)
  lazy val referencesTypeTable = DynamoTable.create(client, Config().referencesTypeTableName)
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
    AWS.Kinesis.putRecord(
      PutRecordRequest.builder()
        .streamName(streamName)
        .data(SdkBytes.fromByteBuffer(dataBuffer))
        .partitionKey(key)
        .build()
    )
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
