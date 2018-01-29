package repositories

import java.net.URI

import com.amazonaws.auth.{AWSCredentialsProvider, AWSCredentialsProviderChain, STSAssumeRoleSessionCredentialsProvider}
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.gu.contentapi.client.{ContentApiClientLogic, GuardianContentApiError, IAMSigner}
import com.gu.contentapi.client.model._
import play.api.Logger
import services.{Config, Contexts}

import scala.annotation.tailrec
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}


object ContentAPI {

  private implicit val executionContext = Contexts.capiContext

  private val previewApiClient = new DraftContentApiClass(Config().capiKey, Config().capiPreviewIAMUrl)

  val capiPreviewCredentials: AWSCredentialsProvider = {
    new AWSCredentialsProviderChain(
      new ProfileCredentialsProvider("capi"),
      new STSAssumeRoleSessionCredentialsProvider.Builder(Config().capiPreviewRole, "capi").build()
    )
  }

  val signer = new IAMSigner(
    credentialsProvider = capiPreviewCredentials,
    awsRegion = Config().aws.region
  )

  def countOccurencesOfTagInContents(contentIds: List[String], apiTagId: String): Int = {
    if (contentIds.nonEmpty) {
      val builder = StringBuilder.newBuilder
      var pageSize = 0

      var total = 0

      for (id <- contentIds) {
        // ~2048 chars is the max sensible amount for a URL.
        // Rather than faff about subtracting our URL + query string junk from 2048 we'll just stay well below the max (1500)
        // Also, CAPI maxes out at 50 items per query
        if (pageSize >= 50 || builder.length + id.length > 1500) {
          total += countTags(builder.toString, pageSize, apiTagId)
          builder.setLength(0)
          pageSize = 0
        }

        if (builder.nonEmpty) {
          builder.append(',')
        }
        builder.append(id)
        pageSize += 1
      }
      total += countTags(builder.toString, pageSize, apiTagId)

      total
    } else 0
  }

  private def countTags(ids: String, pageSize: Int, apiTagId: String): Int = {
    val response = previewApiClient.getResponse(SearchQuery()
      .ids(ids)
      .pageSize(pageSize)
      .showTags("all")
    )
    val contentWithTag = response.map(_.results.filter{ c => c.tags.exists(_.id == apiTagId)})
    val contentWithTagCount = contentWithTag.map(_.length)

    Await.result(contentWithTagCount, 5 seconds)
  }

  def getTag(apiTagId: String) = {

    try {
      val response = previewApiClient.getResponse(ItemQuery(apiTagId))
      Await.result(response.map(_.tag), 5 seconds)
    } catch {
      case GuardianContentApiError(404, _, _) =>
        Logger.debug(s"No tag found for id $apiTagId")
        None
    }
  }

  def countContentWithTag(apiTagId: String, page: Int = 1, count: Int = 0): Int = {
    val response = previewApiClient.getResponse(SearchQuery().tag(apiTagId).pageSize(1))
    val resultPage = Await.result(response, 5 seconds)

    resultPage.total
  }


  @tailrec
  def getContentIdsForTag(apiTagId: String, page: Int = 1, ids: List[String] = Nil): List[String] = {
    Logger.debug(s"Loading page $page of contentent ids for tag $apiTagId")
    val response = previewApiClient.getResponse(SearchQuery().tag(apiTagId).pageSize(100).page(page))

    val resultPage = Await.result(response, 5 seconds)

    val allIds = ids ::: resultPage.results.map(_.id).toList

    if (page >= resultPage.pages) {
      allIds
    } else {
      getContentIdsForTag(apiTagId, page + 1, allIds)
    }
  }

  @tailrec
  def getDraftContentIdsForSection(apiSectionId: String, page: Int = 1, ids: List[String] = Nil): List[String] = {
    Logger.debug(s"Loading page $page of contentent ids for section $apiSectionId")
    val response = previewApiClient.getResponse(SearchQuery().section(apiSectionId).pageSize(100).page(page))

    val resultPage = Await.result(response, 5 seconds)

    val allIds = ids ::: resultPage.results.map(_.id).toList

    if (page >= resultPage.pages) {
      allIds
    } else {
      getDraftContentIdsForSection(apiSectionId, page + 1, allIds)
    }
  }


  def shutdown(): Unit = {
    previewApiClient.shutdown()
  }

}

class DraftContentApiClass(override val apiKey: String, apiUrl: String) extends ContentApiClientLogic() {
  override val targetUrl = apiUrl

  override protected def get(url: String, headers: Map[String, String])(implicit context: ExecutionContext): Future[HttpResponse] = {
    val headersWithAuth = ContentAPI.signer.addIAMHeaders(headers, URI.create(url))
    super.get(url, headersWithAuth)
  }
}
