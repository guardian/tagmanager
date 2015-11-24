package repositories

import com.amazonaws.services.dynamodbv2.document.Item
import model.Section
import model.jobs.Job
import play.api.libs.json.JsValue
import services.Dynamo

import scala.collection.JavaConversions._


object JobRepository {
  def getJob(id: Long) = {
    Option(Dynamo.jobTable.getItem("id", id)).map(Job.fromItem)
  }

  def upsertJob(job: Job) = {
    try {
      Dynamo.jobTable.putItem(job.toItem)
      Some(job)
    } catch {
      case e: Error => None
    }
  }

  def deleteJob(job: Job) {
    deleteJob(job.id)
  }

  def deleteJob(jobId: Long) {
    Dynamo.jobTable.deleteItem("id", jobId)
  }

  def loadAllTags = Dynamo.jobTable.scan().map(Job.fromItem)

}
