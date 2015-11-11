package model.command

import model.command.logic.TagPathCalculator
import repositories.PathManager


class PathUsageCheck(`type`: String, slug: String, sectionId: Option[Long]) extends Command[Map[String, Boolean]] {

  override def process: Option[Map[String, Boolean]] = {
    val calculatedPath = TagPathCalculator calculatePath(`type`, slug, sectionId)

    val inUse = PathManager isPathInUse(calculatedPath)

    Some(Map("inUse" -> inUse))
  }
}
