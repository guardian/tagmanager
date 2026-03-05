package repositories

import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument
import software.amazon.awssdk.services.dynamodb.model._
import model.PillarAudit
import helpers.JodaDateTimeFormat._
import org.joda.time.{DateTime, Duration, ReadableDuration}
import services.Dynamo

import scala.jdk.CollectionConverters._


object PillarAuditRepository {

  def upsertPillarAudit(pillarAudit: PillarAudit): Unit = {
    pillarAudit.logAudit
    Dynamo.pillarAuditTable.putItem(pillarAudit.toItem)
  }

  def getAuditTrailForPillar(pillarId: Long): List[PillarAudit] = {
    val request = QueryRequest.builder()
      .tableName(Dynamo.pillarAuditTable.tableName)
      .keyConditionExpression("pillarId = :pillarId")
      .expressionAttributeValues(Map(":pillarId" -> AttributeValue.builder().n(pillarId.toString).build()).asJava)
      .build()

    val response = Dynamo.client.query(request)
    response.items().asScala.map(item => PillarAudit.fromItem(EnhancedDocument.fromAttributeValueMap(item))).toList
  }

  def getRecentAuditOfPillarOperation(operation: String, timePeriod: ReadableDuration = Duration.standardDays(7)): List[PillarAudit] = {
    val from = new DateTime().minus(timePeriod).getMillis

    val request = QueryRequest.builder()
      .tableName(Dynamo.pillarAuditTable.tableName)
      .indexName("operation-date-index")
      .keyConditionExpression("#op = :operation AND #dt >= :from")
      .expressionAttributeNames(Map("#op" -> "operation", "#dt" -> "date").asJava)
      .expressionAttributeValues(Map(
        ":operation" -> AttributeValue.builder().s(operation).build(),
        ":from" -> AttributeValue.builder().n(from.toString).build()
      ).asJava)
      .build()

    val response = Dynamo.client.query(request)
    response.items().asScala.map(item => PillarAudit.fromItem(EnhancedDocument.fromAttributeValueMap(item))).toList
  }
}
