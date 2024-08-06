package services

import java.util.Properties

import com.amazonaws.services.s3.model.GetObjectRequest
import services.Config._

import scala.jdk.CollectionConverters._


object Config extends AwsInstanceTags {

  lazy val conf = readTag("Stage") match {
    case Some("PROD") => new ProdConfig
    case Some("CODE") => new CodeConfig
    // If in AWS and we don't know our stage, fail fast to avoid ending up running an instance with dev config in PROD!
    case other if instanceId.nonEmpty => throw new IllegalStateException(s"Unable to read Stage tag: $other")
    case _ => new DevConfig
  }

  def apply() = {
    conf
  }
}

sealed trait Config {

  private val remoteConfiguration: Map[String, String] = loadConfiguration

  def capiUrl: String = getRequiredStringProperty("capi.url")
  def capiKey: String = getRequiredStringProperty("capi.key")
  def capiPreviewIAMUrl: String = getRequiredStringProperty("capi.preview.iamUrl")
  def capiPreviewRole: String = getRequiredStringProperty("capi.preview.role")

  def hmacSecret: String = getRequiredStringProperty("hmac.secret")

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
    lazy val region = remoteConfiguration.getOrElse("aws.region", "eu-west-1")
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

    props.asScala.toMap
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
  def pillarsTableName: String
  def pillarsAuditTableName: String

  def referencesTypeTableName: String

  def tagUpdateStreamName: String
  def sectionUpdateStreamName: String
  def taggingOperationsStreamName: String
  def taggingOperationsReIndexStreamName: String
  def pillarUpdateStreamName: String

  def commercialExpiryStreamName: String
  def auditingStreamName: String

  def reindexTagsStreamName: String
  def reindexTagsBatchSize: Int
  def reindexSectionsStreamName: String
  def reindexPillarsStreamName: String

  def reindexProgressTableName: String

  def appAuditTableName: String

  def jobQueueName: String

  def logShippingStreamName: Option[String] = None
  def pandaDomain: String
  val pandaSystemIdentifier: String = "tagmanager"
  val pandaBucketName: String = "pan-domain-auth-settings"
  def pandaSettingsFileKey: String
  def pandaAuthCallback: String

  def composerDomain: String
  def targetingDomain: String
  def workflowDomain: String
  def campaignCentralDomain: String
  def corsableDomains: Seq[String]
  def corsablePostDomains: Seq[String]

  def frontendBucketWriteRole: Option[String] = None
  def tagSearchPageSize = 25
}

class DevConfig extends Config {

  override def tagsTableName: String = "tag-manager-tags-DEV"
  override def sectionsTableName: String = "tag-manager-sections-DEV"
  override def sponsorshipTableName: String = "tag-manager-sponsorships-dev"
  override def sequenceTableName: String = "tag-manager-sequences-dev"
  override def referencesTypeTableName: String = "tag-manager-reference-type-dev"
  override def pillarsTableName: String = "tag-manager-pillars-DEV"
  override def pillarsAuditTableName: String = "tag-manager-pillars-audit-DEV"

  override def jobTableName: String = "tag-manager-background-jobs-dev"
  override def tagAuditTableName: String = "tag-manager-tag-audit-dev"
  override def sectionAuditTableName: String = "tag-manager-section-audit-dev"
  override def clusterStatusTableName: String = "tag-manager-cluster-status-dev"

  override def tagUpdateStreamName: String = "tag-update-stream-dev"
  override def sectionUpdateStreamName: String = "section-update-stream-dev"
  override def taggingOperationsStreamName: String = "tagging-operations-stream-dev"
  override def taggingOperationsReIndexStreamName: String = "tagging-reindex-operations-stream-dev"
  override def commercialExpiryStreamName: String = "commercial-expiry-stream-DEV-KELVIN"
  override def auditingStreamName: String = "auditing-CODE"
  override def pillarUpdateStreamName: String = "pillar-update-stream-CODE"

  override def reindexTagsStreamName: String = "tag-reindex-dev"
  override def reindexTagsBatchSize: Int = 500
  override def reindexSectionsStreamName: String = "section-reindex-dev"
  override def reindexPillarsStreamName: String = "pillar-reindex-CODE"

  override def reindexProgressTableName: String = "tag-manager-reindex-progress-DEV"

  override def appAuditTableName: String = "tag-manager-app-audit-dev"

  override def jobQueueName: String = "tag-manager-job-queue-dev"

  override def logShippingStreamName = Some("elk-CODE-KinesisStream-M03ERGK5PVD9")
  override def pandaDomain: String = "local.dev-gutools.co.uk"
  override def pandaSettingsFileKey: String = "local.dev-gutools.co.uk.settings"
  override def pandaAuthCallback: String = "https://tagmanager.local.dev-gutools.co.uk/oauthCallback"

  override def composerDomain: String = "https://composer.local.dev-gutools.co.uk"
  override def targetingDomain: String = "https://targeting.local.dev-gutools.co.uk"
  override def campaignCentralDomain: String = "https://campaign-central.local.dev-gutools.co.uk"
  override def workflowDomain: String = "https://workflow.local.dev-gutools.co.uk"
  override def corsableDomains: Seq[String] = Seq(
    composerDomain,
    targetingDomain,
    campaignCentralDomain,
    workflowDomain
  )
  override def corsablePostDomains: Seq[String] = Seq(
    targetingDomain
  )
}

class CodeConfig extends Config {
  override def tagsTableName: String = "tag-manager-tags-CODE"
  override def sectionsTableName: String = "tag-manager-sections-CODE"
  override def sponsorshipTableName: String = "tag-manager-sponsorships-CODE"
  override def sequenceTableName: String = "tag-manager-sequences-CODE"
  override def referencesTypeTableName: String = "tag-manager-reference-type-CODE"
  override def pillarsTableName: String = "tag-manager-pillars-CODE"
  override def pillarsAuditTableName: String = "tag-manager-pillars-audit-CODE"

  override def jobTableName: String = "tag-manager-background-jobs-CODE"
  override def tagAuditTableName: String = "tag-manager-tag-audit-CODE"
  override def sectionAuditTableName: String = "tag-manager-section-audit-CODE"
  override def clusterStatusTableName: String = "tag-manager-cluster-status-CODE"

  override def tagUpdateStreamName: String = "tag-update-stream-CODE"
  override def sectionUpdateStreamName: String = "section-update-stream-CODE"
  override def taggingOperationsStreamName: String = "tagging-operations-stream-CODE"
  override def taggingOperationsReIndexStreamName: String = "tagging-reindex-operations-stream-CODE"
  override def commercialExpiryStreamName: String = "commercial-expiry-stream-CODE"
  override def auditingStreamName: String = "auditing-CODE"
  override def pillarUpdateStreamName: String = "pillar-update-stream-CODE"


  override def reindexTagsStreamName: String = "tag-reindex-CODE"
  override def reindexTagsBatchSize: Int = 500
  override def reindexSectionsStreamName: String = "section-reindex-CODE"
  override def reindexPillarsStreamName: String = "pillar-reindex-CODE"

  override def reindexProgressTableName: String = "tag-manager-reindex-progress-CODE"

  override def appAuditTableName: String = "tag-manager-app-audit-CODE"

  override def jobQueueName: String = "tag-manager-job-queue-CODE"

  override def logShippingStreamName = Some("elk-PROD-KinesisStream-1PYU4KS1UEQA")
  override def pandaDomain: String = "code.dev-gutools.co.uk"
  override def pandaSettingsFileKey: String = "code.dev-gutools.co.uk.settings"
  override def pandaAuthCallback: String = "https://tagmanager.code.dev-gutools.co.uk/oauthCallback"

  override def composerDomain: String = "https://composer.code.dev-gutools.co.uk"
  override def targetingDomain: String = "https://targeting.code.dev-gutools.co.uk"
  override def campaignCentralDomain: String = "https://campaign-central.code.dev-gutools.co.uk"
  override def workflowDomain: String = "https://workflow.code.dev-gutools.co.uk"

  override def corsableDomains: Seq[String] = Seq(
    composerDomain,
    "https://composer-secondary.code.dev-gutools.co.uk",
    "https://composer.local.dev-gutools.co.uk",
    targetingDomain,
    "https://targeting.local.dev-gutools.co.uk",
    campaignCentralDomain,
    "https://campaign-central.local.dev-gutools.co.uk",
    workflowDomain,
    "https://workflow.local.dev-gutools.co.uk"
    )

  override def frontendBucketWriteRole: Option[String] = Some("arn:aws:iam::642631414762:role/composerWriteToStaticBucket")
  override def corsablePostDomains: Seq[String] = Seq(
    targetingDomain,
    "https://targeting.local.dev-gutools.co.uk"
  )
}

class ProdConfig extends Config {
  override def tagsTableName: String = "tag-manager-tags-PROD"
  override def sectionsTableName: String = "tag-manager-sections-PROD"
  override def sponsorshipTableName: String = "tag-manager-sponsorships-PROD"
  override def sequenceTableName: String = "tag-manager-sequences-PROD"
  override def referencesTypeTableName: String = "tag-manager-reference-type-PROD"
  override def pillarsTableName: String = "tag-manager-pillars-PROD"
  override def pillarsAuditTableName: String = "tag-manager-pillars-audit-CODE"

  override def jobTableName: String = "tag-manager-background-jobs-PROD"
  override def tagAuditTableName: String = "tag-manager-tag-audit-PROD"
  override def sectionAuditTableName: String = "tag-manager-section-audit-PROD"
  override def clusterStatusTableName: String = "tag-manager-cluster-status-PROD"
  override def auditingStreamName: String = "auditing-PROD"
  override def pillarUpdateStreamName: String = "pillar-update-stream-PROD"

  override def tagUpdateStreamName: String = "tag-update-stream-PROD"
  override def sectionUpdateStreamName: String = "section-update-stream-PROD"
  override def taggingOperationsStreamName: String = "tagging-operations-stream-PROD"
  override def taggingOperationsReIndexStreamName: String = "tagging-reindex-operations-stream-PROD"
  override def commercialExpiryStreamName: String = "commercial-expiry-stream-PROD"

  override def reindexTagsStreamName: String = "tag-reindex-PROD"
  override def reindexTagsBatchSize: Int = 500
  override def reindexSectionsStreamName: String = "section-reindex-PROD"
  override def reindexPillarsStreamName: String = "pillar-reindex-PROD"

  override def reindexProgressTableName: String = "tag-manager-reindex-progress-PROD"

  override def appAuditTableName: String = "tag-manager-app-audit-PROD"

  override def jobQueueName: String = "tag-manager-job-queue-PROD"

  override def logShippingStreamName = Some("elk-PROD-KinesisStream-1PYU4KS1UEQA")
  override def pandaDomain: String = "gutools.co.uk"
  override def pandaSettingsFileKey: String = "gutools.co.uk.settings"
  override def pandaAuthCallback: String = "https://tagmanager.gutools.co.uk/oauthCallback"

  override def composerDomain: String = "https://composer.gutools.co.uk"
  override def targetingDomain: String = "https://targeting.gutools.co.uk"
  override def campaignCentralDomain: String = "https://campaign-central.gutools.co.uk"
  override def workflowDomain: String = "https://workflow.gutools.co.uk"
  override def corsableDomains: Seq[String] = Seq(
    composerDomain,
    targetingDomain,
    campaignCentralDomain,
    workflowDomain,
    "https://composer-secondary.gutools.co.uk"
    )

  override def frontendBucketWriteRole: Option[String] = Some("arn:aws:iam::642631414762:role/composerWriteToStaticBucket")
  override def corsablePostDomains: Seq[String] = Seq(
    targetingDomain
  )
}
