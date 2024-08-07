package repositories

import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition
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
    Dynamo.appAuditTable.getIndex("operation-date-index")
      .query("operation", operation, new RangeKeyCondition("date").ge(from))
      .asScala
      .map(AppAudit.fromItem).toList
  }
}
