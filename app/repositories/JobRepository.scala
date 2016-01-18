package repositories

import com.amazonaws.services.dynamodbv2.document.{ScanFilter, Item}
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

  def loadAllJobs = Dynamo.jobTable.scan().map(Job.fromItem).toList

  def findJobsForTag(tagId: Long): List[Job] = {
    Dynamo.jobTable.scan(new ScanFilter("tagIds").contains(tagId)).map(Job.fromItem).toList
  }


  private val getType: String => List[Job] = `type` => loadAllJobs.filter(_.`type` == `type`)
  val getMerges: List[Job] = getType("Merge tag")
  val getDeletes: List[Job] = getType("Delete tag")
}
