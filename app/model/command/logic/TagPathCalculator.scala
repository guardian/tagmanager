package model.command.logic

import model.command.CommandError._
import repositories.SectionRepository


object TagPathCalculator {

  def calculatePath(tagType: String, slug: String, sectionId: Option[Long], tagSubType: Option[String]) = {

    val loadedSection = sectionId.map(SectionRepository.getSection(_).getOrElse(SectionNotFound))

    val sectionPathPrefix = loadedSection.map(_.wordsForUrl + "/").getOrElse("")

    (tagType.toLowerCase, tagSubType.map(_.toLowerCase)) match {
      case ("contenttype", _) => s"$slug"
      case ("tone", _) => s"tone/$slug"
      case ("contributor", _) => s"profile/$slug"
      case ("publication", _) => s"$slug/all"
      case ("series", _) => s"${sectionPathPrefix}series/$slug"
      case ("tracking", trackingType) => s"tracking/${trackingType.getOrElse("")}/$slug"
      case ("campaign", campaignType) => s"campaign/${campaignType.getOrElse("")}/$slug"
      case ("paidcontent", Some("hostedcontent")) => s"advertiser-content/$slug"
      case (_, _) => sectionPathPrefix + slug
    }
  }

}
