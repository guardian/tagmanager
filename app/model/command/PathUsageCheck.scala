package model.command

import model.command.logic.TagPathCalculator
import repositories.PathManager

import scala.concurrent.{Future, ExecutionContext}


class PathUsageCheck(tagType: String, slug: String, sectionId: Option[Long], tagSubType: Option[String]) extends Command {

  type T = Map[String, Boolean]

  override def process()(implicit username: Option[String] = None, ec: ExecutionContext): Future[Some[Map[String, Boolean]]] = Future{
    val calculatedPath = TagPathCalculator calculatePath(tagType, slug, sectionId, tagSubType)

    val inUse = PathManager isPathInUse(calculatedPath)

    Some(Map("inUse" -> inUse))
  }
}
