package model.command

import model.jobs.JobHelper

import scala.concurrent.{Future, ExecutionContext}

case class ReindexPillarsCommand() extends Command {
  override type T = Unit

  override def process()(implicit username: Option[String], ec: ExecutionContext): Future[Option[T]] = Future{
    JobHelper.beginPillarReindex()
    Some(())
  }
}
