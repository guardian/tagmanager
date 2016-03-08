package model.command

import play.api.Logger
import play.api.libs.json._
import play.api.libs.functional.syntax._

trait Command {
  type T

  def process()(implicit username: Option[String] = None): Option[T]
}
