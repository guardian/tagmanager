package model.command

import model.jobs.JobHelper
import services.Contexts

import scala.concurrent.Future

case class ReindexSectionsCommand() extends Command {
  override type T = Unit

  override def process()(implicit username: Option[String] = None): Future[Option[T]] = Future{
    JobHelper.beginSectionReindex
    Some(())
  }(Contexts.tagOperationContext)
}
