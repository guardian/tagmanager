package model.command

import scala.concurrent.Future

trait Command {
  type T

  def process()(implicit username: Option[String] = None): Future[Option[T]]
}
