package repositories

import model.Pillar
import play.api.Logging
import services.Dynamo

import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}


object PillarRepository extends Logging {
  def getPillar(id: Long): Option[Pillar] = {
    Option(Dynamo.pillarTable.getItem("id", id)).map(Pillar.fromItem)
  }

  def updatePillar(pillar: Pillar): Option[Pillar] = {
    try {
      Dynamo.pillarTable.putItem(Pillar.toItem(pillar))
      Some(pillar)
    } catch {
      case e: Error =>
        logger.warn(s"Error updating pillar ${pillar.id}: ${e.getMessage}", e)
        None
    }
  }

  def deletePillar(id: Long): Option[Long] = {
    Try(Dynamo.pillarTable.deleteItem("id", id)) match {
      case Success(_) => Some(id)
      case Failure(e) =>
        logger.warn(s"Error deleting pillar $id: ${e.getMessage}", e)
        None
    }
  }

  def loadAllPillars: Iterable[Pillar] = Dynamo.pillarTable.scan().asScala.map(Pillar.fromItem)

  def count = Dynamo.pillarTable.scan().asScala.count(_ => true)
}
