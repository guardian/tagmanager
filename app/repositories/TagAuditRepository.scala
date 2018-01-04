package repositories

import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition
import model.TagAudit
import org.joda.time.{DateTime, Duration, ReadableDuration}
import play.api.Logger
import services.Dynamo

import scala.collection.JavaConversions._

object TagAuditRepository {
  def upsertTagAudit(tagAudit: TagAudit): Unit = {
    Logger.info(s"User '${tagAudit.user}' performed a '${tagAudit.operation}' tag operation: '${tagAudit.description}'")
    Dynamo.tagAuditTable.putItem(tagAudit.toItem)
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

  def getAuditsOfTagOperationsSince(operation: String, since: Long): List[TagAudit] = {
    Dynamo.tagAuditTable.getIndex("operation-date-index")
      .query("operation", operation, new RangeKeyCondition("date").ge(since))
      .map(TagAudit.fromItem).toList
  }

  def loadAllAudits: List[TagAudit] = Dynamo.tagAuditTable.scan().map(TagAudit.fromItem).toList

  val lastModifiedTags: Long => List[TagAudit] = since => loadAllAudits
    .filter(x => x.operation == "updated" && x.date.getMillis > since)

  private val getType: String => List[TagAudit] = operation => loadAllAudits.filter(_.operation == operation)
  lazy val getMerges: List[TagAudit] = getType("merged")
  lazy val getDeletes: List[TagAudit] = getType("deleted")
  lazy val getCreates: List[TagAudit] = getType("created")
}
