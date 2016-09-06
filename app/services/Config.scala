package services

import java.util.Properties

import com.amazonaws.services.s3.model.GetObjectRequest
import services.Config._

import scala.collection.JavaConversions._


object Config extends AwsInstanceTags {

  lazy val conf = readTag("Stage") match {
    case Some("PROD") =>    new ProdConfig
    case Some("CODE") =>    new CodeConfig
    case _ =>               new DevConfig
  }

  def apply() = {
    conf
  }
}

sealed trait Config {

  private val remoteConfiguration: Map[String, String] = loadConfiguration

  def capiUrl: String = getRequiredStringProperty("capi.url")
  def capiKey: String = getRequiredStringProperty("capi.key")
  def capiPreviewUrl: String = getRequiredStringProperty("capi.preview.url")
  def capiPreviewUser: String = getRequiredStringProperty("capi.preview.username")
  def capiPreviewPassword: String = getRequiredStringProperty("capi.preview.password")

  def pathManagerUrl: String = getRequiredStringProperty("pathmanager.url")

  def getRequiredStringProperty(key: String): String = {
    remoteConfiguration.getOrElse(key, {
      throw new IllegalArgumentException(s"Property '$key' not configured")
    })
  }

  object aws {
    lazy val stack = readTag("Stack") getOrElse "flexible"
    lazy val stage = readTag("Stage") getOrElse "DEV"
    lazy val app = readTag("App") getOrElse "tag-manager"
  }

  private def loadConfiguration = {

    val bucketName = s"guconf-${aws.stack}"

    def loadPropertiesFromS3(propertiesKey: String, props: Properties): Unit = {
      val s3Properties = AWS.S3Client.getObject(new GetObjectRequest(bucketName, propertiesKey))
      val propertyInputStream = s3Properties.getObjectContent
      try {
        props.load(propertyInputStream)
      } finally {
        try {propertyInputStream.close()} catch {case _: Throwable => /*ignore*/}
      }
    }

    val props = new Properties()

    loadPropertiesFromS3(s"${aws.app}/global.properties", props)
    loadPropertiesFromS3(s"${aws.app}/${aws.stage}.properties", props)

    props.toMap
  }

  lazy val permissionsStage: String = readTag("Stage") match {
    case Some("DEV") =>  "CODE"
    case Some(stage) =>  stage
    case _           =>  "CODE"
  }

  def tagsTableName: String
  def sectionsTableName: String
  def sponsorshipTableName: String
  def sequenceTableName: String
  def jobTableName: String
  def tagAuditTableName: String
  def sectionAuditTableName: String
  def clusterStatusTableName: String

  def referencesTypeTableName: String

  def tagUpdateStreamName: String
  def sectionUpdateStreamName: String
  def taggingOperationsStreamName: String

  def commercialExpiryStreamName: String
  def auditingStreamName: String

  def reindexTagsStreamName: String
  def reindexTagsBatchSize: Int
  def reindexSectionsStreamName: String

  def reindexProgressTableName: String

  def appAuditTableName: String

  def jobQueueName: String

  def logShippingStreamName: Option[String] = None
  def pandaDomain: String
  def pandaAuthCallback: String

  def composerDomain: String
  def corsableDomains: Seq[String]

  def frontendBucketWriteRole: Option[String] = None
  def auditingKinesisWriteRole: Option[String] = None
  def enableAuditStreaming: Boolean = true
}

class DevConfig extends Config {

  override def tagsTableName: String = "tag-manager-tags-DEV"
  override def sectionsTableName: String = "tag-manager-sections-DEV"
  override def sponsorshipTableName: String = "tag-manager-sponsorships-dev"
  override def sequenceTableName: String = "tag-manager-sequences-dev"
  override def referencesTypeTableName: String = "tag-manager-reference-type-dev"

  override def jobTableName: String = "tag-manager-background-jobs-dev"
  override def tagAuditTableName: String = "tag-manager-tag-audit-dev"
  override def sectionAuditTableName: String = "tag-manager-section-audit-dev"
  override def clusterStatusTableName: String = "tag-manager-cluster-status-dev"

  override def tagUpdateStreamName: String = "tag-update-stream-dev"
  override def sectionUpdateStreamName: String = "section-update-stream-dev"
  override def taggingOperationsStreamName: String = "tagging-operations-stream-dev"
  override def commercialExpiryStreamName: String = "commercial-expiry-stream-DEV-KELVIN"
  override def auditingStreamName: String = "auditing-CODE"

  override def reindexTagsStreamName: String = "tag-reindex-dev"
  override def reindexTagsBatchSize: Int = 500
  override def reindexSectionsStreamName: String = "section-reindex-dev"

  override def reindexProgressTableName: String = "tag-manager-reindex-progress-DEV"

  override def appAuditTableName: String = "tag-manager-app-audit-dev"

  override def jobQueueName: String = "tag-manager-job-queue-dev"

  override def logShippingStreamName = Some("elk-CODE-KinesisStream-M03ERGK5PVD9")
  override def pandaDomain: String = "local.dev-gutools.co.uk"
  override def pandaAuthCallback: String = "https://tagmanager.local.dev-gutools.co.uk/oauthCallback"

  override def composerDomain: String = "https://composer.local.dev-gutools.co.uk"
  override def corsableDomains: Seq[String] = Seq(composerDomain, "https://targeting.local.dev-gutools.co.uk")

  //Disables submission of audits to the audit Kinesis server, requires frontCms credentials locally to enable
  override def enableAuditStreaming: Boolean = false
}

class CodeConfig extends Config {

  override def tagsTableName: String = "tag-manager-tags-CODE"
  override def sectionsTableName: String = "tag-manager-sections-CODE"
  override def sponsorshipTableName: String = "tag-manager-sponsorships-CODE"
  override def sequenceTableName: String = "tag-manager-sequences-CODE"
  override def referencesTypeTableName: String = "tag-manager-reference-type-CODE"

  override def jobTableName: String = "tag-manager-background-jobs-CODE"
  override def tagAuditTableName: String = "tag-manager-tag-audit-CODE"
  override def sectionAuditTableName: String = "tag-manager-section-audit-CODE"
  override def clusterStatusTableName: String = "tag-manager-cluster-status-CODE"

  override def tagUpdateStreamName: String = "tag-update-stream-CODE"
  override def sectionUpdateStreamName: String = "section-update-stream-CODE"
  override def taggingOperationsStreamName: String = "tagging-operations-stream-CODE"
  override def commercialExpiryStreamName: String = "commercial-expiry-stream-CODE"
  override def auditingStreamName: String = "auditing-CODE"


  override def reindexTagsStreamName: String = "tag-reindex-CODE"
  override def reindexTagsBatchSize: Int = 500
  override def reindexSectionsStreamName: String = "section-reindex-CODE"

  override def reindexProgressTableName: String = "tag-manager-reindex-progress-CODE"

  override def appAuditTableName: String = "tag-manager-app-audit-CODE"

  override def jobQueueName: String = "tag-manager-job-queue-CODE"

  override def logShippingStreamName = Some("elk-PROD-KinesisStream-1PYU4KS1UEQA")
  override def pandaDomain: String = "code.dev-gutools.co.uk"
  override def pandaAuthCallback: String = "https://tagmanager.code.dev-gutools.co.uk/oauthCallback"

  override def composerDomain: String = "https://composer.code.dev-gutools.co.uk"
  override def corsableDomains: Seq[String] = Seq(
    composerDomain,
    "https://composer-secondary.code.dev-gutools.co.uk",
    "https://composer.local.dev-gutools.co.uk",
    "https://targeting.code.dev-gutools.co.uk",
    "https://targeting.local.dev-gutools.co.uk")

  override def frontendBucketWriteRole: Option[String] = Some("arn:aws:iam::642631414762:role/composerWriteToStaticBucket")
  override def auditingKinesisWriteRole: Option[String] = Some("arn:aws:iam::163592447864:role/auditing-CrossAccountKinesisAccess-CC5UXEHZNP5M")
}

class ProdConfig extends Config {


  override def tagsTableName: String = "tag-manager-tags-PROD"
  override def sectionsTableName: String = "tag-manager-sections-PROD"
  override def sponsorshipTableName: String = "tag-manager-sponsorships-PROD"
  override def sequenceTableName: String = "tag-manager-sequences-PROD"
  override def referencesTypeTableName: String = "tag-manager-reference-type-PROD"

  override def jobTableName: String = "tag-manager-background-jobs-PROD"
  override def tagAuditTableName: String = "tag-manager-tag-audit-PROD"
  override def sectionAuditTableName: String = "tag-manager-section-audit-PROD"
  override def clusterStatusTableName: String = "tag-manager-cluster-status-PROD"
  override def auditingStreamName: String = "auditing-PROD"

  override def tagUpdateStreamName: String = "tag-update-stream-PROD"
  override def sectionUpdateStreamName: String = "section-update-stream-PROD"
  override def taggingOperationsStreamName: String = "tagging-operations-stream-PROD"
  override def commercialExpiryStreamName: String = "commercial-expiry-stream-PROD"

  override def reindexTagsStreamName: String = "tag-reindex-PROD"
  override def reindexTagsBatchSize: Int = 500
  override def reindexSectionsStreamName: String = "section-reindex-PROD"

  override def reindexProgressTableName: String = "tag-manager-reindex-progress-PROD"

  override def appAuditTableName: String = "tag-manager-app-audit-PROD"

  override def jobQueueName: String = "tag-manager-job-queue-PROD"

  override def logShippingStreamName = Some("elk-PROD-KinesisStream-1PYU4KS1UEQA")
  override def pandaDomain: String = "gutools.co.uk"
  override def pandaAuthCallback: String = "https://tagmanager.gutools.co.uk/oauthCallback"

  override def composerDomain: String = "https://composer.gutools.co.uk"
  override def corsableDomains: Seq[String] = Seq(composerDomain, "https://composer-secondary.gutools.co.uk", "https://targeting.gutools.co.uk")

  override def frontendBucketWriteRole: Option[String] = Some("arn:aws:iam::642631414762:role/composerWriteToStaticBucket")
  override def auditingKinesisWriteRole: Option[String] = Some("arn:aws:iam::163592447864:role/auditing-CrossAccountKinesisAccess-CC5UXEHZNP5M")

}
