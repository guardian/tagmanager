package model.command

import com.gu.tagmanagement.{TagWithSection, OperationType, TaggingOperation}
import model.command.CommandError._
import model.jobs.JobHelper
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Format}
import repositories._
import services.{SQS, KinesisStreams}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


case class DeleteTagCommand(removingTagId: Long) extends Command {
  override type T = Unit

  override def process()(implicit username: Option[String] = None): Option[T] = {
    val removingTag = TagRepository.getTag(removingTagId) getOrElse(TagNotFound)

    JobHelper.beginTagDeletion(removingTag)

    Some(())
  }
}
