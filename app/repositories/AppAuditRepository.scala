package repositories

import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument
import software.amazon.awssdk.services.dynamodb.model._
import model.AppAudit
import org.joda.time.{Duration, ReadableDuration, DateTime}
import services.Dynamo

import scala.jdk.CollectionConverters._

object AppAuditRepository {
  def upsertAppAudit(appAudit: AppAudit): Unit = {
    appAudit.logAudit
    Dynamo.appAuditTable.putItem(appAudit.toItem)
  }

  def getRecentAuditOfOperation(operation: String, timePeriod: ReadableDuration = Duration.standardDays(7)): List[AppAudit] = {
    val from = new DateTime().minus(timePeriod).getMillis

    val request = QueryRequest.builder()
      .tableName(Dynamo.appAuditTable.tableName)
      .indexName("operation-date-index")
      .keyConditionExpression("#op = :operation AND #dt >= :from")
      .expressionAttributeNames(Map("#op" -> "operation", "#dt" -> "date").asJava)
      .expressionAttributeValues(Map(
        ":operation" -> AttributeValue.builder().s(operation).build(),
        ":from" -> AttributeValue.builder().n(from.toString).build()
      ).asJava)
      .build()

    val response = Dynamo.client.query(request)
    response.items().asScala.map(item => AppAudit.fromItem(EnhancedDocument.fromAttributeValueMap(item))).toList
  }
}
