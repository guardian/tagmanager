package repositories

import com.squareup.okhttp.{FormEncodingBuilder, OkHttpClient, Request}
import play.api.Logger
import play.api.libs.json.Json
import services.Config
import java.util.concurrent.TimeUnit


case class PathRegistrationFailed(m: String) extends RuntimeException(m)
case class PathRemoveFailed(m: String) extends RuntimeException(m)

object PathManager {

  val httpClient = new OkHttpClient()


  def registerPathAndGetPageId(path: String): Long = {
    Logger.info(s"registering path $path")

    val formBody = new FormEncodingBuilder()
      .add("path", path)
      .add("system", "tagmanager")
      .build()
    val req = new Request.Builder()
      .url(s"${Config().pathManagerUrl}paths")
      .post(formBody)
      .build

    val resp = httpClient.newCall(req).execute

    resp.code match {
      case 200 => {
        val responseJson = Json.parse(resp.body().string())
        val pathId = (responseJson \ "data" \ "canonical" \\ "identifier").head.as[Long]

        Logger.info(s"path $path registered with pathId $pathId")

        pathId
      }
      case c => {
        Logger.warn(s"failed to register $path. response code $c, message ${resp.body}")
        throw PathRegistrationFailed(s"failed to register $path. response code $c, message ${resp.body}")
      }
    }
  }

  def removePathForId(id: Long) = {
    Logger.info(s"removing path entries for $id")

    val req = new Request.Builder().url(s"${Config().pathManagerUrl}paths/$id").delete().build
    val resp = httpClient.newCall(req).execute

    resp.code match {
      case 204 => Logger.info(s"path entries for $id removed")
      case c => {
        Logger.warn(s"failed to remove paths for $id. response code $c, message ${resp.body}")
        throw new PathRemoveFailed(s"failed to remove paths for $id. response code $c, message ${resp.body}")
      }
    }
  }

  def isPathInUse(path: String): Boolean = {

    httpClient.setConnectTimeout(5, TimeUnit.SECONDS)
    val req = new Request.Builder().url(s"${Config().pathManagerUrl}paths?path=$path").build
    val resp = httpClient.newCall(req).execute

    resp.code match {
      case 404 => false // not found = not in use
      case 200 => true
      case c => {
        Logger.warn(s"failed to check path $path in use. response code $c, message ${resp.body}")
        throw new RuntimeException(s"failed to check path $path in use. response code $c, message ${resp.body}")
      }
    }
  }
}
