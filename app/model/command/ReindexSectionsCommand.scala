package model.command

import model.jobs.JobHelper

import scala.concurrent.{Future, ExecutionContext}

case class ReindexSectionsCommand() extends Command {
  override type T = Unit

  override def process()(implicit username: Option[String] = None, ec: ExecutionContext): Future[Option[T]] = Future{
    JobHelper.beginSectionReindex
    Some(())
  }
}
