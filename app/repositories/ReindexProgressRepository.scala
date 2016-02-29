package repositories;

import model.ReindexProgress
import services.Dynamo

object ReindexProgressRepository {
  // Write
  def resetTagReindexProgress(expectedDocs: Int) = {
    Dynamo.reindexProgressTable.putItem(ReindexProgress.resetTag(expectedDocs).toItem)
  }

  def resetSectionReindexProgress(expectedDocs: Int) = {
    Dynamo.reindexProgressTable.putItem(ReindexProgress.resetSection(expectedDocs).toItem)
  }

  def updateTagReindexProgress(docsSent: Int, docsTotal: Int) = {
    Dynamo.reindexProgressTable.putItem(ReindexProgress.progressTag(docsSent, docsTotal).toItem)
  }

  def updateSectionReindexProgress(docsSent: Int, docsTotal: Int) = {
    Dynamo.reindexProgressTable.putItem(ReindexProgress.progressSection(docsSent, docsTotal).toItem)
  }

  def completeTagReindex(docsSent: Int, docsTotal: Int) = {
    Dynamo.reindexProgressTable.putItem(ReindexProgress.completeTag(docsSent, docsTotal).toItem)
  }

  def completeSectionReindex(docsSent: Int, docsTotal: Int) = {
    Dynamo.reindexProgressTable.putItem(ReindexProgress.completeSection(docsSent, docsTotal).toItem)
  }

  def failTagReindex(docsSent: Int, docsTotal: Int) = {
    Dynamo.reindexProgressTable.putItem(ReindexProgress.failTag(docsSent, docsTotal).toItem)
  }

  def failSectionReindex(docsSent: Int, docsTotal: Int) = {
    Dynamo.reindexProgressTable.putItem(ReindexProgress.failSection(docsSent, docsTotal).toItem)
  }

  // Read
  def getTagReindexProgress(): Option[ReindexProgress] = {
    Option(Dynamo.reindexProgressTable.getItem("type", ReindexProgress.TagTypeName))
      .map(ReindexProgress.fromItem(_))
  }

  def getSectionReindexProgress(): Option[ReindexProgress] = {
    Option(Dynamo.reindexProgressTable.getItem("type", ReindexProgress.SectionTypeName))
      .map(ReindexProgress.fromItem(_))
  }

  def isTagReindexInProgress(): Boolean = {
    getTagReindexProgress.map(_.status == ReindexProgress.InProgress).getOrElse(false)
  }

  def isSectionReindexInProgress(): Boolean = {
    getSectionReindexProgress.map(_.status == ReindexProgress.InProgress).getOrElse(false)
  }
}
