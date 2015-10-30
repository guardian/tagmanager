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

  def updateSection(sectionJson: JsValue) = {
    try {
      val section = Section.fromJson(sectionJson) // parsing input json here provides bugjet validation
      Dynamo.sectionTable.putItem(Item.fromJSON(sectionJson.toString()))
      Some(section)
    } catch {
      case e: Error => None
    }
  }

  def loadAllSections = Dynamo.sectionTable.scan().map(Section.fromItem)

}
