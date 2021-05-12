package repositories

import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition
import model.SectionAudit
import play.api.libs.json.JodaWrites._
import play.api.libs.json.JodaReads._
import org.joda.time.{DateTime, Duration, ReadableDuration}
import play.api.Logging
import services.Dynamo

import scala.collection.JavaConversions._


object SectionAuditRepository {

  def upsertSectionAudit(sectionAudit: SectionAudit): Unit = {
    sectionAudit.logAudit
    Dynamo.sectionAuditTable.putItem(sectionAudit.toItem)
  }

  def getAuditTrailForSection(sectionId: Long): List[SectionAudit] = {
    Dynamo.sectionAuditTable.query("sectionId", sectionId).map(SectionAudit.fromItem).toList
  }

  def getRecentAuditOfSectionOperation(operation: String, timePeriod: ReadableDuration = Duration.standardDays(7)): List[SectionAudit] = {
    val from = new DateTime().minus(timePeriod).getMillis
    Dynamo.sectionAuditTable.getIndex("operation-date-index")
      .query("operation", operation, new RangeKeyCondition("date").ge(from))
      .map(SectionAudit.fromItem).toList
  }
}
