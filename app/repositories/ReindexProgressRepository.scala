package repositories;

import model.ReindexProgress
import services.{Contexts, Dynamo}

import scala.concurrent.Future
import services.Contexts.tagOperationContext

object ReindexProgressRepository {
  // Write
  def resetTagReindexProgress(expectedDocs: Int) = {
    Dynamo.reindexProgressTable.putItem(ReindexProgress.resetTag(expectedDocs).toItem)
  }

  def resetSectionReindexProgress(expectedDocs: Int) = {
    Dynamo.reindexProgressTable.putItem(ReindexProgress.resetSection(expectedDocs).toItem)
  }

  def resetPillarReindexProgress(expectedDocs: Int) = {
    Dynamo.reindexProgressTable.putItem(ReindexProgress.resetPillar(expectedDocs).toItem)
  }

  def updateTagReindexProgress(docsSent: Int, docsTotal: Int) = {
    Dynamo.reindexProgressTable.putItem(ReindexProgress.progressTag(docsSent, docsTotal).toItem)
  }

  def updateSectionReindexProgress(docsSent: Int, docsTotal: Int) = {
    Dynamo.reindexProgressTable.putItem(ReindexProgress.progressSection(docsSent, docsTotal).toItem)
  }
  
  def updatePillarReindexProgress(docsSent: Int, docsTotal: Int) = {
    Dynamo.reindexProgressTable.putItem(ReindexProgress.progressPillar(docsSent, docsTotal).toItem)
  }

  def completeTagReindex(docsSent: Int, docsTotal: Int) = {
    Dynamo.reindexProgressTable.putItem(ReindexProgress.completeTag(docsSent, docsTotal).toItem)
  }

  def completeSectionReindex(docsSent: Int, docsTotal: Int) = {
    Dynamo.reindexProgressTable.putItem(ReindexProgress.completeSection(docsSent, docsTotal).toItem)
  }
  
  def completePillarReindex(docsSent: Int, docsTotal: Int) = {
    Dynamo.reindexProgressTable.putItem(ReindexProgress.completePillar(docsSent, docsTotal).toItem)
  }

  def failTagReindex(docsSent: Int, docsTotal: Int) = {
    Dynamo.reindexProgressTable.putItem(ReindexProgress.failTag(docsSent, docsTotal).toItem)
  }

  def failSectionReindex(docsSent: Int, docsTotal: Int) = {
    Dynamo.reindexProgressTable.putItem(ReindexProgress.failSection(docsSent, docsTotal).toItem)
  }
  
  def failPillarReindex(docsSent: Int, docsTotal: Int) = {
    Dynamo.reindexProgressTable.putItem(ReindexProgress.failPillar(docsSent, docsTotal).toItem)
  }

  // Read
  def getTagReindexProgress: Future[Option[ReindexProgress]] = {
    Future{
      Option(Dynamo.reindexProgressTable.getItem("type", ReindexProgress.TagTypeName))
        .map(ReindexProgress.fromItem)
    }
  }

  def getSectionReindexProgress: Future[Option[ReindexProgress]] = {
    Future{
      Option(Dynamo.reindexProgressTable.getItem("type", ReindexProgress.SectionTypeName))
        .map(ReindexProgress.fromItem)
    }
  }

  def getPillarReindexProgress: Future[Option[ReindexProgress]] = {
    Future{
      Option(Dynamo.reindexProgressTable.getItem("type", ReindexProgress.PillarTypeName))
        .map(ReindexProgress.fromItem)
    }
  }

  def isTagReindexInProgress: Future[Boolean] = {
    getTagReindexProgress.map { result =>
      result.exists(_.status == ReindexProgress.InProgress)
    }
  }

  def isSectionReindexInProgress: Future[Boolean] = {
    getSectionReindexProgress.map { result =>
      result.exists(_.status == ReindexProgress.InProgress)
    }
  }

  def isPillarReindexInProgress: Future[Boolean] = {
    getPillarReindexProgress.map { result =>
      result.exists(_.status == ReindexProgress.InProgress)
    }
  }
}
