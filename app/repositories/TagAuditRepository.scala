package repositories

import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument
import software.amazon.awssdk.services.dynamodb.model._
import model.TagAudit
import helpers.JodaDateTimeFormat._
import org.joda.time.{DateTime, Duration, ReadableDuration}
import play.api.Logging
import services.Dynamo

import scala.jdk.CollectionConverters._

object TagAuditRepository {
  def upsertTagAudit(tagAudit: TagAudit): Unit = {
    tagAudit.logAudit
    Dynamo.tagAuditTable.putItem(tagAudit.toItem)
  }

  def getAuditTrailForTag(tagId: Long): List[TagAudit] = {
    val request = QueryRequest.builder()
      .tableName(Dynamo.tagAuditTable.tableName)
      .keyConditionExpression("tagId = :tagId")
      .expressionAttributeValues(Map(":tagId" -> AttributeValue.builder().n(tagId.toString).build()).asJava)
      .build()

    val response = Dynamo.client.query(request)
    response.items().asScala.map(item => TagAudit.fromItem(EnhancedDocument.fromAttributeValueMap(item))).toList
  }

  def getRecentAuditOfTagOperation(operation: String, timePeriod: ReadableDuration = Duration.standardDays(31)): List[TagAudit] = {
    val from = new DateTime().minus(timePeriod).getMillis

    val request = QueryRequest.builder()
      .tableName(Dynamo.tagAuditTable.tableName)
      .indexName("operation-date-index")
      .keyConditionExpression("#op = :operation AND #dt >= :from")
      .expressionAttributeNames(Map("#op" -> "operation", "#dt" -> "date").asJava)
      .expressionAttributeValues(Map(
        ":operation" -> AttributeValue.builder().s(operation).build(),
        ":from" -> AttributeValue.builder().n(from.toString).build()
      ).asJava)
      .build()

    val response = Dynamo.client.query(request)
    response.items().asScala
      .map(item => TagAudit.fromItem(EnhancedDocument.fromAttributeValueMap(item)))
      .toList
  }

  def getAuditsOfTagOperationsSince(operation: String, since: Long): List[TagAudit] = {
    val request = QueryRequest.builder()
      .tableName(Dynamo.tagAuditTable.tableName)
      .indexName("operation-date-index")
      .keyConditionExpression("#op = :operation AND #dt >= :since")
      .expressionAttributeNames(Map("#op" -> "operation", "#dt" -> "date").asJava)
      .expressionAttributeValues(Map(
        ":operation" -> AttributeValue.builder().s(operation).build(),
        ":since" -> AttributeValue.builder().n(since.toString).build()
      ).asJava)
      .build()

    val response = Dynamo.client.query(request)
    response.items().asScala.map(item => TagAudit.fromItem(EnhancedDocument.fromAttributeValueMap(item))).toList
  }

  def loadAllAudits: List[TagAudit] = Dynamo.tagAuditTable.scan().map(TagAudit.fromItem).toList

  val lastModifiedTags: Long => List[TagAudit] = since => loadAllAudits
    .filter(x => x.operation == "updated" && x.date.getMillis > since)

  private val getType: String => List[TagAudit] = operation => loadAllAudits.filter(_.operation == operation)
  lazy val getMerges: List[TagAudit] = getType("merged")
  lazy val getDeletes: List[TagAudit] = getType("deleted")
  lazy val getCreates: List[TagAudit] = getType("created")
}
