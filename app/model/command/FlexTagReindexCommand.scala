package model.command

import com.gu.tagmanagement.{OperationType, TaggingOperation}
import model.Tag
import play.api.Logger
import repositories._
import services.{KinesisStreams}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class FlexTagReindexCommand(tag: Tag) extends Command {

  type T = Tag

  override def process()(implicit username: Option[String] = None): Option[T] = {

    Future {
      val contentIds = ContentAPI.getContentIdsForTag(tag.path)

      contentIds foreach { contentPath =>
        val taggingOperation = TaggingOperation(
          operation = OperationType.Reindex,
          contentPath = contentPath
        )
        Logger.info(s"raising flex reindex for content $contentPath")
        KinesisStreams.taggingOperationsStream.publishUpdate(contentPath.take(200), taggingOperation)
      }
    }

    Some(tag)

  }
}
