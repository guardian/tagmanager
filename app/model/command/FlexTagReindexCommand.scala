package model.command

import com.gu.tagmanagement.{OperationType, TaggingOperation}
import model.Tag
import play.api.Logging
import repositories._
import services.KinesisStreams

import scala.concurrent.{Future, ExecutionContext}

case class FlexTagReindexCommand(tag: Tag) extends Command with Logging {

  type T = Tag

  override def process()(implicit username: Option[String] = None, ec: ExecutionContext): Future[Option[T]] = {
    Future {
      val contentIds = ContentAPI.getContentIdsForTag(tag.path)

      contentIds foreach { contentPath =>
        val taggingOperation = TaggingOperation(
          operation = OperationType.Reindex,
          contentPath = contentPath
        )
        logger.info(s"raising flex reindex for content $contentPath")
        KinesisStreams.taggingOperationsReIndexStream.publishUpdate(contentPath.take(200), taggingOperation)
      }
      Some(tag)
    }
  }
}
