package model.command

import model.command.logic.TagPathCalculator
import repositories.PathManager
import services.Contexts

import scala.concurrent.Future


class PathUsageCheck(tagType: String, slug: String, sectionId: Option[Long], tagSubType: Option[String]) extends Command {

  type T = Map[String, Boolean]

  override def process()(implicit username: Option[String] = None): Future[Some[Map[String, Boolean]]] = Future{
    val calculatedPath = TagPathCalculator calculatePath(tagType, slug, sectionId, tagSubType)

    val inUse = PathManager isPathInUse(calculatedPath)

    Some(Map("inUse" -> inUse))
  }(Contexts.tagOperationContext)
}
