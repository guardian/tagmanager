package repositories

import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition
import model.PillarAudit
import org.joda.time.{DateTime, Duration, ReadableDuration}
import play.api.Logger
import services.Dynamo

import scala.collection.JavaConversions._


object PillarAuditRepository {

  def upsertPillarAudit(pillarAudit: PillarAudit) = {
    try {
      Dynamo.pillarAuditTable.putItem(pillarAudit.toItem)
      Some(pillarAudit)
    } catch {
      case e: Error =>
        Logger.warn(s"Error updating pillar ${pillarAudit.pillarId}: ${e.getMessage}", e)
        None
    }
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
