package model.command

import model.command.CommandError._
import model.jobs.JobHelper
import repositories._
import services.Contexts

import scala.concurrent.Future


case class DeleteTagCommand(removingTagId: Long) extends Command {
  override type T = Unit

  override def process()(implicit username: Option[String] = None): Future[Option[T]] = Future{
    val removingTag = TagRepository.getTag(removingTagId) getOrElse(TagNotFound)

    JobHelper.beginTagDeletion(removingTag)

    Some(())
  }(Contexts.tagOperationContext)
}
