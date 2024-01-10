package model.command

import scala.concurrent.{ExecutionContext, Future}

trait Command {
  type T

  def process()(implicit username: Option[String], ec: ExecutionContext): Future[Option[T]]
}
