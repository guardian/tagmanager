package repositories

import com.amazonaws.services.dynamodbv2.document._
import com.amazonaws.services.dynamodbv2.document.spec._
import com.amazonaws.services.dynamodbv2.document.utils._
import com.amazonaws.services.dynamodbv2.model._
import model.Section
import model.jobs.{Job, JobStatus}
import play.api.libs.json.JsValue
import services.Dynamo

import scala.collection.JavaConversions._


object JobRepository {
  def getJob(id: Long) = {
    Option(Dynamo.jobTable.getItem("id", id)).map(Job.fromItem)
  }

  def lock(job: Job, nodeId: String): Option[Job] = {
    val updateItemSpec = new UpdateItemSpec()
      .withPrimaryKey("id", job.id)
      .withReturnValues(ReturnValue.ALL_NEW)
      .withUpdateExpression("SET owner = :newOwner, status = :ownedStatus")
      .withConditionExpression("attribute_not_exists(owner) AND status = :waitingStatus")
      .withValueMap(new ValueMap()
        .withString(":newOwner", nodeId)
        .withString(":ownedStatus", JobStatus.owned)
        .withString(":waitingStatus", JobStatus.waiting))

    val outcome = Job.fromItem(Dynamo.jobTable.updateItem(updateItemSpec).getItem())

    if (outcome.owner == nodeId) {
      Some(outcome)
    } else {
      None
    }
  }

  def unlock(job: Job, nodeId: String) = {
    val updateItemSpec = new UpdateItemSpec()
      .withPrimaryKey("id", job.id)
      .withUpdateExpression("REMOVE owner SET status = :waitingStatus")
      .withConditionExpression("owner = :currentOwner")
      .withValueMap(new ValueMap()
        .withString(":currentOwner", nodeId)
        .withString(":waitingStatus", JobStatus.waiting))

      Dynamo.jobTable.updateItem(updateItemSpec)
  }

  def addJob(job: Job) = {
    Dynamo.jobTable.putItem(job.toItem)
  }

  /** You can only modify a job if you own it*/
  def updateJobIfOwned(job: Job, nodeId: String) = {
    // TODO Implement
  }

  def deleteIfCompleteOrFailed(id: Long) = {
    // TODO Implement
  }

  def loadAllJobs = Dynamo.jobTable.scan().map(Job.fromItem(_)).toList

  def findJobsForTag(tagId: Long): List[Job] = {
    Dynamo.jobTable.scan(new ScanFilter("tagIds").contains(tagId)).map(Job.fromItem).toList
  }
}
