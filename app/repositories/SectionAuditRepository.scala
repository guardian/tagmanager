package repositories

import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition
import model.SectionAudit
import org.joda.time.{DateTime, Duration, ReadableDuration}
import services.Dynamo

import scala.collection.JavaConversions._


object SectionAuditRepository {

  def upsertSectionAudit(sectionAudit: SectionAudit) = {
    try {
      Dynamo.sectionAuditTable.putItem(sectionAudit.toItem)
      Some(sectionAudit)
    } catch {
      case e: Error => None
    }
  }

  def getAuditTrailForSection(sectionId: Long): List[SectionAudit] = {
    Dynamo.sectionAuditTable.query("sectionId", sectionId).map(SectionAudit.fromItem).toList
  }

  def getRecentAuditOfOperation(operation: String, timePeriod: ReadableDuration = Duration.standardDays(7)): List[SectionAudit] = {
    val from = new DateTime().minus(timePeriod).getMillis
    Dynamo.tagAuditTable.getIndex("operation-date-index")
      .query("operation", operation, new RangeKeyCondition("date").ge(from))
      .map(SectionAudit.fromItem).toList
  }

}
