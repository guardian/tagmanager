package model.command

import model.AppAudit;
import model.jobs.JobHelper
import services.SQS
import org.cvogt.play.json.Jsonx
import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.Logger
import repositories._

case class ReindexTagsCommand() extends Command {
  override type T = Unit

  override def process()(implicit username: Option[String] = None): Option[T] = {
    JobHelper.beginTagReindex
    Some(())
  }
}

