package model.command.logic

import model.command.CommandError._
import repositories.SectionRepository


object TagPathCalculator {

  def calculatePath(tagType: String, slug: String, sectionId: Option[Long], tagSubType: Option[String]) = {

    val loadedSection = sectionId.map(SectionRepository.getSection(_).getOrElse(SectionNotFound))

    val sectionPathPrefix = loadedSection.map(_.wordsForUrl + "/").getOrElse("")
    val trackingTagName = tagSubType.getOrElse("")

    tagType.toLowerCase match {
      case "contenttype" => s"$slug"
      case "tone" => s"tone/$slug"
      case "contributor" => s"profile/$slug"
      case "publication" => s"$slug/all"
      case "series" => s"${sectionPathPrefix}series/$slug"
      case "tracking" => s"tracking/${trackingTagName}/$slug"
      case _ => sectionPathPrefix + slug
    }
  }

}
