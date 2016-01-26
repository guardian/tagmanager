package repositories

import com.amazonaws.services.dynamodbv2.document.Item
import model.Section
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

}

object SectionLookupCache {

  // a map makes this faster to lookup
  private val m = Map[Long,Section]()
  val allSections = new AtomicReference[Map[Long, Section]](m)

  def refresh = {
    val sections = SectionRepository.loadAllSections
    sections.foreach { section =>
      insertSection(section)
    }
  }

  def getSection(id: Option[Long]): Option[Section] = {
    id.flatMap { i =>
      allSections.get.get(i)
    }
  }

  def insertSection(section: Section): Map[Long, Section] = {
    val current = allSections.get
    allSections.getAndSet(current + (section.id -> section))
  }

  def removeSection(id: Long): Map[Long, Section] = {
    val current = allSections.get
    allSections.getAndSet(current - id)
  }
}
