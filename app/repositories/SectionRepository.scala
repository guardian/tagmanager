package repositories

import com.amazonaws.services.dynamodbv2.document.Item
import model.Section
import play.api.Logger
import play.api.libs.json.JsValue
import services.Dynamo
import scala.collection.JavaConversions._
import java.util.concurrent.atomic.AtomicReference


object SectionRepository {
  def getSection(id: Long) = {
    Option(Dynamo.sectionTable.getItem("id", id)).map(Section.fromItem)
  }

  def updateSection(section: Section) = {
    try {
      Dynamo.sectionTable.putItem(section.toItem)
      Some(section)
    } catch {
      case e: Error => None
    }
  }

  def loadAllSections = Dynamo.sectionTable.scan().map(Section.fromItem)

  def count = Dynamo.sectionTable.scan().count(_ => true)

}

object SectionLookupCache {

  // a map makes this faster to lookup
  val allSections = new AtomicReference[Map[Long, Section]](Map())

  def refresh = {
    SectionRepository.loadAllSections.foreach { section =>
      insertSection(section)
    }
  }

  def getSection(id: Option[Long]): Option[Section] = {
    id.flatMap { i =>
      allSections.get.get(i)
    }
  }

  def insertSection(section: Section) = {
    var currentSections: Map[Long, Section] = null
    var newSections: Map[Long, Section] = null

    var count = 0

    Logger.info(s"Attempting to insert section (${section.id}) into cache")
    do {
      currentSections = allSections.get
      newSections = currentSections + (section.id -> section)
      count += 1
    } while (!allSections.compareAndSet(currentSections, newSections))

    Logger.info(s"Successfully inserted section (${section.id}) into cache after ${count} attempts")
  }

  def removeSection(id: Long) = {
    var currentSections: Map[Long, Section] = null
    var newSections: Map[Long, Section] = null

    var count = 0;

    Logger.info(s"Attempting to remove section (${id}) from cache")
    do {
      currentSections = allSections.get
      newSections = currentSections - id
    } while (!allSections.compareAndSet(currentSections, newSections))
    Logger.info(s"Successfully removed section (${id}) from cache after ${count} attempts")
  }
}
