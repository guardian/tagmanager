package model.command.logic

import model.command.CommandError._
import model.Section

object SectionEditionPathCalculator {

  def calculatePath(section: Section, editionRegion: String) = {

    editionRegion match {
      case "US" => s"us/${section.wordsForUrl}"
      case "UK" => s"uk/${section.wordsForUrl}"
      case "AU" => s"au/${section.wordsForUrl}"
      case _ => throw InvalidSectionEditionRegion
    }
  }

}
