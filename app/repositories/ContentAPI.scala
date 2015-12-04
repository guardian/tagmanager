package repositories

import java.util.concurrent.Executors

import com.gu.contentapi.client.ContentApiClientLogic
import com.gu.contentapi.client.model._
import services.Config

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._


object ContentAPI {

  private val executorService = Executors.newFixedThreadPool(25)
  private implicit val executionContext = ExecutionContext.fromExecutor(executorService)

  private val apiClient = new LiveContentApiClass(Config().capiKey, Config().capiUrl)

  def countOccurencesOfTagInContents(contentIds: List[String], apiTagId: String): Int= {
    val response = apiClient.getResponse(new SearchQuery()
      .ids(contentIds mkString(","))
      .pageSize(contentIds.length)
      .showTags("all")
    )

    val contentWithTag = response.map(_.results.filter{ c => c.tags.exists(_.id == apiTagId)})

    val contentWithTagCount = contentWithTag.map(_.length)

    val count = Await.result(contentWithTagCount, 5 seconds)
    count
  }


  def shutdown: Unit = {
    apiClient.shutdown()
    executorService.shutdown()
  }

}

class LiveContentApiClass(override val apiKey: String, apiUrl: String) extends ContentApiClientLogic() {

  override val targetUrl = apiUrl
}


