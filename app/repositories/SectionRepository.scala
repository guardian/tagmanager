package repositories

import com.amazonaws.services.dynamodbv2.document.Item
import model.Section
import play.api.libs.json.JsValue
import services.Dynamo
import scala.collection.JavaConversions._


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
