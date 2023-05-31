package repositories

import com.amazonaws.services.dynamodbv2.document._
import com.amazonaws.services.dynamodbv2.document.spec._
import com.amazonaws.services.dynamodbv2.document.utils._
import com.amazonaws.services.dynamodbv2.model._
import model.Section
import model.jobs.{Job, JobStatus}
import play.api.libs.json.JsValue

import scala.util.control.NonFatal
import services.Dynamo

import scala.collection.convert.ImplicitConversions._


object JobRepository {
  def getJob(id: Long) = {
    Option(Dynamo.jobTable.getItem("id", id)).map(Job.fromItem)
  }

  def lock(job: Job, nodeId: String, currentTime: Long, lockBreakTime: Long): Option[Job] = {
    val updateItemSpec = new UpdateItemSpec()
      .withPrimaryKey("id", job.id)
      .withReturnValues(ReturnValue.ALL_NEW)
      .withUpdateExpression("SET ownedBy = :newOwner, lockedAt = :lockedAt, jobStatus = :ownedStatus")
      .withConditionExpression("(attribute_not_exists(ownedBy) AND jobStatus <> :ownedStatus) OR (attribute_exists(ownedBy) AND lockedAt < :lockBreakTime)")
      .withValueMap(new ValueMap()
        .withString(":newOwner", nodeId)
        .withLong(":lockedAt", currentTime)
        .withLong(":lockBreakTime", lockBreakTime)
        .withString(":ownedStatus", JobStatus.owned))

    try {
      Some(Job.fromItem(Dynamo.jobTable.updateItem(updateItemSpec).getItem()))
    } catch {
      case NonFatal(e) => {
        println(e) // Someone else got here first
        None
      }
    }
  }

  def unlock(job: Job, nodeId: String) = {
    val status = if (job.jobStatus == JobStatus.owned) {
      JobStatus.waiting
    } else { // If we've failed or completed then we don't want to set the stauts to waiting
      job.jobStatus
    }

    val updateItemSpec = new UpdateItemSpec()
      .withPrimaryKey("id", job.id)
      .withUpdateExpression("REMOVE ownedBy SET jobStatus = :status")
      .withConditionExpression("ownedBy = :currentOwner")
      .withValueMap(new ValueMap()
        .withString(":currentOwner", nodeId)
        .withString(":status", status))

    try {
      Dynamo.jobTable.updateItem(updateItemSpec)
    } catch {
      case NonFatal(e) => {
        println(e) // Should only happen if someone stole our lock, at which point we can't really do anything
      }
    }
  }

  def addJob(job: Job) = {
    Dynamo.jobTable.putItem(job.toItem)
  }

  /** You can only modify a job if you own it */
  def upsertJobIfOwned(job: Job, nodeId: String) = {
    val putItemSpec = new PutItemSpec()
      .withItem(job.toItem)
      .withConditionExpression("ownedBy = :currentOwner")
      .withValueMap(new ValueMap()
        .withString(":currentOwner", nodeId))

    try {
      Dynamo.jobTable.putItem(putItemSpec)
    } catch {
      case NonFatal(e) => {
        println(e) // Should only happen if someone stole our lock, at which point we can't really do anything
      }
    }
  }

  /** Delete a job if it's in a terminal state: complete, failed or rolled back */
  def deleteIfTerminal(id: Long) = {
    val deleteItemSpec = new DeleteItemSpec()
      .withPrimaryKey("id", id)
      .withConditionExpression("jobStatus = :complete OR jobStatus = :failed OR jobStatus = :rolledback")
      .withValueMap(new ValueMap()
        .withString(":complete", JobStatus.complete)
        .withString(":rolledback", JobStatus.rolledback)
        .withString(":failed", JobStatus.failed))

    Dynamo.jobTable.deleteItem(deleteItemSpec)
  }

  def loadAllJobs = {
    Dynamo.jobTable.scan().map(Job.fromItem(_)).toList
  }

  def findJobsForTag(tagId: Long): List[Job] = {
    Dynamo.jobTable.scan(new ScanFilter("tagIds").contains(tagId)).map(Job.fromItem).toList
  }
}
