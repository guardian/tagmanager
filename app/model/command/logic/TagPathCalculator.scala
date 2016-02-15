package model.command.logic

import model.command.CommandError._
import repositories.SectionLookupCache


object TagPathCalculator {

  def calculatePath(`type`: String, slug: String, sectionId: Option[Long]) = {

    val loadedSection = sectionId.map(SectionLookupCache.getSection(_).getOrElse(SectionNotFound))

    val sectionPathPrefix = loadedSection.map(_.wordsForUrl + "/").getOrElse("")

    `type`.toLowerCase match {
      case "contenttype" => s"$slug"
      case "tone" => s"tone/$slug"
      case "contributor" => s"profile/$slug"
      case "publication" => s"$slug/all"
      case "series" => s"${sectionPathPrefix}series/$slug"
      case _ => sectionPathPrefix + slug
    }
  }

}
