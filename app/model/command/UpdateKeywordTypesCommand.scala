package model.command

import model.jobs.JobHelper
import model.jobs.steps.UpdateKeywordTypeForAllTags

import scala.concurrent.{ExecutionContext, Future}

case class UpdateKeywordTypesCommand(keywordTypeMappings: Map[Long, String]) extends Command {
  override type T = Unit

  override def process()(implicit username: Option[String], ec: ExecutionContext): Future[Option[T]] = Future {
    JobHelper.beginUpdateKeywordTypes(keywordTypeMappings)
    Some(())
  }
}

object UpdateKeywordTypesCommand {
  def fromCsv(csvContent: String): UpdateKeywordTypesCommand = {
    val step = UpdateKeywordTypeForAllTags.fromCsv(csvContent)
    UpdateKeywordTypesCommand(step.keywordTypeMappings)
  }
}

