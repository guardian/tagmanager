package model.command

import com.gu.pandomainauth.model.User
import model.command.logic.TagPathCalculator
import repositories.PathManager


class PathUsageCheck(`type`: String, slug: String, sectionId: Option[Long]) extends Command {

  type T = Map[String, Boolean]

  override def process()(implicit user: Option[User] = None): Option[Map[String, Boolean]] = {
    val calculatedPath = TagPathCalculator calculatePath(`type`, slug, sectionId)

    val inUse = PathManager isPathInUse(calculatedPath)

    Some(Map("inUse" -> inUse))
  }
}
