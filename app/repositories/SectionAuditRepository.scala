package repositories

import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument
import software.amazon.awssdk.services.dynamodb.model._
import model.SectionAudit
import helpers.JodaDateTimeFormat._
import org.joda.time.{DateTime, Duration, ReadableDuration}
import play.api.Logging
import services.Dynamo

import scala.jdk.CollectionConverters._


object SectionAuditRepository {

  def upsertSectionAudit(sectionAudit: SectionAudit): Unit = {
    sectionAudit.logAudit
    Dynamo.sectionAuditTable.putItem(sectionAudit.toItem)
  }

  def getAuditTrailForSection(sectionId: Long): List[SectionAudit] = {
    val request = QueryRequest.builder()
      .tableName(Dynamo.sectionAuditTable.tableName)
      .keyConditionExpression("sectionId = :sectionId")
      .expressionAttributeValues(Map(":sectionId" -> AttributeValue.builder().n(sectionId.toString).build()).asJava)
      .build()

    val response = Dynamo.client.query(request)
    response.items().asScala.map(item => SectionAudit.fromItem(EnhancedDocument.fromAttributeValueMap(item))).toList
  }

  def getRecentAuditOfSectionOperation(operation: String, timePeriod: ReadableDuration = Duration.standardDays(7)): List[SectionAudit] = {
    val from = new DateTime().minus(timePeriod).getMillis

    val request = QueryRequest.builder()
      .tableName(Dynamo.sectionAuditTable.tableName)
      .indexName("operation-date-index")
      .keyConditionExpression("#op = :operation AND #dt >= :from")
      .expressionAttributeNames(Map("#op" -> "operation", "#dt" -> "date").asJava)
      .expressionAttributeValues(Map(
        ":operation" -> AttributeValue.builder().s(operation).build(),
        ":from" -> AttributeValue.builder().n(from.toString).build()
      ).asJava)
      .build()

    val response = Dynamo.client.query(request)
    response.items().asScala.map(item => SectionAudit.fromItem(EnhancedDocument.fromAttributeValueMap(item))).toList
  }
}
