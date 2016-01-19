package repositories

import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition
import model.TagAudit
import org.joda.time.{Duration, ReadableDuration, DateTime}
import services.Dynamo

import scala.collection.JavaConversions._


object TagAuditRepository {

  def upsertTagAudit(tagAudit: TagAudit) = {
    try {
      Dynamo.tagAuditTable.putItem(tagAudit.toItem)
      Some(tagAudit)
    } catch {
      case e: Error => None
    }
  }

  def getAuditTrailForTag(tagId: Long): List[TagAudit] = {
    Dynamo.tagAuditTable.query("tagId", tagId).map(TagAudit.fromItem).toList
  }

  def getRecentAuditOfTagOperation(operation: String, timePeriod: ReadableDuration = Duration.standardDays(31)): List[TagAudit] = {
    val from = new DateTime().minus(timePeriod).getMillis
    Dynamo.tagAuditTable.getIndex("operation-date-index")
      .query("operation", operation, new RangeKeyCondition("date").ge(from))
      .map(TagAudit.fromItem).toList
  }

  def loadAllAudits: List[TagAudit] = Dynamo.tagAuditTable.scan().map(TagAudit.fromItem).toList
  val lastModifiedTags: Long => List[TagAudit] = since => loadAllAudits
    .filter(x => (x.operation == "updated" && x.date.getMillis > since))
}
