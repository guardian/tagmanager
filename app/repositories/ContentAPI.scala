package repositories

import java.util.concurrent.Executors

import com.gu.contentapi.client.{ContentApiClientLogic, GuardianContentApiError}
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

    try {
      val response = apiClient.getResponse(new ItemQuery(apiTagId))
      Await.result(response.map(_.tag), 5 seconds)
    } catch {
      case GuardianContentApiError(404, _, _) => {
        Logger.debug(s"No tag found for id ${apiTagId}")
        None
      }
    }
  }

  @tailrec
  def countContentWithTag(apiTagId: String, page: Int = 1, count: Int = 0): Int = {
    val response = apiClient.getResponse(new SearchQuery().tag(apiTagId).pageSize(100).page(page).showFields("internalComposerCode"))

    val resultPage = Await.result(response, 5 seconds)

    val newCount = count + resultPage.results.count((result) => {
      result.fields match {
        case Some(fields) => {
          fields.internalComposerCode match {
            case Some(_) => true
            case None => false
          }
        }
        case None => false
      }
    })

    if (page >= resultPage.pages) {
      newCount
    } else {
      Logger.debug(s"Found ${count + newCount} pieces of content so far...")
      countContentWithTag(apiTagId, page + 1, newCount)
    }

  }


  @tailrec
  def getContentIdsForTag(apiTagId: String, page: Int = 1, ids: List[String] = Nil): List[String] = {
    Logger.debug(s"Loading page ${page} of contentent ids for tag ${apiTagId}")
    val response = apiClient.getResponse(new SearchQuery().tag(apiTagId).pageSize(100).page(page))

    val resultPage = Await.result(response, 5 seconds)

    val allIds = ids ::: resultPage.results.map(_.id)

    if (page >= resultPage.pages) {
      allIds
    } else {
      getContentIdsForTag(apiTagId, page + 1, allIds)
    }
  }

  @tailrec
  def getContentIdsForSection(apiSectionId: String, page: Int = 1, ids: List[String] = Nil): List[String] = {
    Logger.debug(s"Loading page ${page} of contentent ids for section ${apiSectionId}")
    val response = apiClient.getResponse(new SearchQuery().section(apiSectionId).pageSize(100).page(page))

    val resultPage = Await.result(response, 5 seconds)

    val allIds = ids ::: resultPage.results.map(_.id)

    if (page >= resultPage.pages) {
      allIds
    } else {
      getContentIdsForSection(apiSectionId, page + 1, allIds)
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


