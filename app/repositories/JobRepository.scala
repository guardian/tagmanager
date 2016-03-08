package repositories

import com.amazonaws.services.dynamodbv2.document._
import com.amazonaws.services.dynamodbv2.document.spec._
import model.Section
import model.jobs.Job
import play.api.libs.json.JsValue
import services.Dynamo

import scala.collection.JavaConversions._


object JobRepository {
  def getJob(id: Long) = {
    Option(Dynamo.jobTable.getItem("id", id)).map(Job.fromItem)
  }

  def lock(job: Job, nodeId: String): Boolean = {
    val updateItemSpec = new UpdateItemSpec()
      .withUpdateExpression(s"SET owner = $nodeId")
      .withConditionExpression(s"attribute_not_exists(owner) AND status = waiting")

    val updateResult = Dynamo.jobTable.updateItem(updateItemSpec)

    // TODO deal with dynamos consistency guarantees? ?? !??!?! ?! ?? !?
    // Should be able to get the updateItem response to know if everthing went well...
    // at this point we should know if we successfully took the lock

    false
  }

  def unlock(job: Job) = {
    val updateItemSpec = new UpdateItemSpec()
      .withUpdateExpression("REMOVE owner")
      .withConditionExpression(s"owner = ${job.owner.get}")

    // TODO Call spec
  }

  def addJob(job: Job) = {
    // TODO Implement
    // Add a job it a job with that id doesnt already exist
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
