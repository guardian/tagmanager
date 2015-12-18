package repositories

import java.util.concurrent.Executors

import com.gu.contentapi.client.ContentApiClientLogic
import com.gu.contentapi.client.model._
import play.api.Logger
import services.Config

import scala.annotation.tailrec
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

  def getTag(apiTagId: String) = {
    val response = apiClient.getResponse(new ItemQuery(apiTagId))

    Await.result(response.map(_.tag), 5 seconds)
  }

  def countContentWithTag(apiTagId: String) = {
    val response = apiClient.getResponse(new SearchQuery().tag(apiTagId).pageSize(1))
    Await.result(response.map(_.total), 5 seconds)
  }


  @tailrec
  def getContentIdsForTag(apiTagId: String, page: Int = 1, ids: List[String] = Nil): List[String] = {
    Logger.debug(s"Loading page ${page} of contentent ids for tag ${apiTagId}")
    val response = apiClient.getResponse(new SearchQuery().tag(apiTagId).pageSize(100).page(page))

    val resultPage = Await.result(response, 5 seconds)

    val allIds = ids ::: resultPage.results.map(_.id)

    if (page == resultPage.pages) {
      allIds
    } else {
      getContentIdsForTag(apiTagId, page + 1, allIds)
    }
  }


  def shutdown: Unit = {
    apiClient.shutdown()
    executorService.shutdown()
  }

}

class LiveContentApiClass(override val apiKey: String, apiUrl: String) extends ContentApiClientLogic() {
  override val targetUrl = apiUrl
}

class DraftContentApiClass(override val apiKey: String, apiUrl: String) extends ContentApiClientLogic() {
  override val targetUrl = apiUrl
}


