package model.command

import model.command.CommandError._
import model.jobs.JobHelper
import repositories._

import scala.concurrent.{Future, ExecutionContext}


case class DeleteTagCommand(removingTagId: Long) extends Command {
  override type T = Unit

  override def process()(implicit username: Option[String] = None, ec: ExecutionContext): Future[Option[T]] = Future{
    val removingTag = TagRepository.getTag(removingTagId) getOrElse(TagNotFound)

    JobHelper.beginTagDeletion(removingTag)

    Some(())
  }
}
