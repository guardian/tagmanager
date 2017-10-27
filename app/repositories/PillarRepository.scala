package repositories

import model.Pillar
import play.api.Logger
import services.Dynamo

import scala.collection.JavaConversions._


object PillarRepository {
  def getPillar(id: Long): Option[Pillar] = {
    Option(Dynamo.pillarTable.getItem("id", id)).map(Pillar.fromItem)
  }

  def updatePillar(pillar: Pillar): Option[Pillar] = {
    try {
      Dynamo.pillarTable.putItem(Pillar.toItem(pillar))
      Some(pillar)
    } catch {
      case e: Error =>
        Logger.warn(s"Error updating pillar ${pillar.id}: ${e.getMessage}", e)
        None
    }
  }

  def loadAllPillars: Iterable[Pillar] = Dynamo.pillarTable.scan().map(Pillar.fromItem)
}
