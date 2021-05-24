package repositories

import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition
import model.PillarAudit
import helpers.JodaDateTimeFormat._
import org.joda.time.{DateTime, Duration, ReadableDuration}
import services.Dynamo

import scala.collection.JavaConversions._


object PillarAuditRepository {

  def upsertPillarAudit(pillarAudit: PillarAudit): Unit = {
    pillarAudit.logAudit
    Dynamo.pillarAuditTable.putItem(pillarAudit.toItem)
  }

  def getAuditTrailForPillar(pillarId: Long): List[PillarAudit] = {
    Dynamo.pillarAuditTable.query("pillarId", pillarId).map(PillarAudit.fromItem).toList
  }

  def getRecentAuditOfPillarOperation(operation: String, timePeriod: ReadableDuration = Duration.standardDays(7)): List[PillarAudit] = {
    val from = new DateTime().minus(timePeriod).getMillis
    Dynamo.pillarAuditTable.getIndex("operation-date-index")
      .query("operation", operation, new RangeKeyCondition("date").ge(from))
      .map(PillarAudit.fromItem).toList
  }
}
