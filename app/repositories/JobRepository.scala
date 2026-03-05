package repositories

import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument
import software.amazon.awssdk.services.dynamodb.model._
import model.Section
import model.jobs.{Job, JobStatus}
import play.api.libs.json.{JsValue, Json}
import scala.util.control.NonFatal
import services.{Dynamo, DynamoJsonConversions}

import scala.jdk.CollectionConverters._


object JobRepository {
  def getJob(id: Long) = {
    Dynamo.jobTable.getItem("id", id).map(Job.fromItem)
  }

  def lock(job: Job, nodeId: String, currentTime: Long, lockBreakTime: Long): Option[Job] = {
    try {
      val response = Dynamo.jobTable.updateItem(
        key = Map("id" -> AttributeValue.builder().n(job.id.toString).build()),
        updateExpression = "SET ownedBy = :newOwner, lockedAt = :lockedAt, jobStatus = :ownedStatus",
        expressionAttributeValues = Map(
          ":newOwner" -> AttributeValue.builder().s(nodeId).build(),
          ":lockedAt" -> AttributeValue.builder().n(currentTime.toString).build(),
          ":lockBreakTime" -> AttributeValue.builder().n(lockBreakTime.toString).build(),
          ":ownedStatus" -> AttributeValue.builder().s(JobStatus.owned).build()
        ),
        conditionExpression = Some("(attribute_not_exists(ownedBy) AND jobStatus <> :ownedStatus) OR (attribute_exists(ownedBy) AND lockedAt < :lockBreakTime)")
      )
      if (response.hasAttributes) {
        Some(Job.fromItem(EnhancedDocument.fromAttributeValueMap(response.attributes())))
      } else None
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

    try {
      Dynamo.jobTable.updateItem(
        key = Map("id" -> AttributeValue.builder().n(job.id.toString).build()),
        updateExpression = "REMOVE ownedBy SET jobStatus = :status",
        expressionAttributeValues = Map(
          ":currentOwner" -> AttributeValue.builder().s(nodeId).build(),
          ":status" -> AttributeValue.builder().s(status).build()
        ),
        conditionExpression = Some("ownedBy = :currentOwner")
      )
    } catch {
      case NonFatal(e) => {
        println(e) // Should only happen if someone stole our lock, at which point we can't really do anything
      }
    }
  }

  def addJob(job: Job) = {
    Dynamo.jobTable.putItem(DynamoJsonConversions.jsonToDocument(Json.toJson(job)))
  }

  /** You can only modify a job if you own it */
  def upsertJobIfOwned(job: Job, nodeId: String) = {
    try {
      val request = PutItemRequest.builder()
        .tableName(Dynamo.jobTable.tableName)
        .item(DynamoJsonConversions.jsonToDocument(Json.toJson(job)).toMap)
        .conditionExpression("ownedBy = :currentOwner")
        .expressionAttributeValues(Map(":currentOwner" -> AttributeValue.builder().s(nodeId).build()).asJava)
        .build()
      Dynamo.client.putItem(request)
    } catch {
      case NonFatal(e) => {
        println(e) // Should only happen if someone stole our lock, at which point we can't really do anything
      }
    }
  }

  /** Delete a job if it's in a terminal state: complete, failed or rolled back */
  def deleteIfTerminal(id: Long) = {
    try {
      val request = DeleteItemRequest.builder()
        .tableName(Dynamo.jobTable.tableName)
        .key(Map("id" -> AttributeValue.builder().n(id.toString).build()).asJava)
        .conditionExpression("jobStatus = :complete OR jobStatus = :failed OR jobStatus = :rolledback")
        .expressionAttributeValues(Map(
          ":complete" -> AttributeValue.builder().s(JobStatus.complete).build(),
          ":rolledback" -> AttributeValue.builder().s(JobStatus.rolledback).build(),
          ":failed" -> AttributeValue.builder().s(JobStatus.failed).build()
        ).asJava)
        .build()
      Dynamo.client.deleteItem(request)
    } catch {
      case _: ConditionalCheckFailedException => // Expected if job not in terminal state
    }
  }

  def loadAllJobs = {
    Dynamo.jobTable.scan().map(Job.fromItem).toList
  }

  def findJobsForTag(tagId: Long): List[Job] = {
    // Filter in memory - scan all and filter
    loadAllJobs.filter(_.tagIds.contains(tagId))
  }
}
