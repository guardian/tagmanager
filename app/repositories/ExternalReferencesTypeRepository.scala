package repositories

import com.amazonaws.services.dynamodbv2.document.Item
import model.ExternalReferenceType
import play.api.libs.json.JsValue
import services.Dynamo

import scala.collection.JavaConversions._


object ExternalReferencesTypeRepository {
  def getReferenceType(typeName: String) = {
    Option(Dynamo.referencesTypeTable.getItem("typeName", typeName)).map(ExternalReferenceType.fromItem)
  }

  def updateReferenceType(referenceTypeJson: JsValue) = {
    try {
      val ref = ExternalReferenceType.fromJson(referenceTypeJson) // parsing input json here provides bugjet validation
      Dynamo.referencesTypeTable.putItem(Item.fromJSON(referenceTypeJson.toString()))
      Some(ref)
    } catch {
      case e: Error => None
    }
  }

  def loadAllReferenceTypes = Dynamo.referencesTypeTable.scan().map(ExternalReferenceType.fromItem)

}
