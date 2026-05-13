package repositories

import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument
import model.ExternalReferenceType
import play.api.libs.json.JsValue
import services.{Dynamo, DynamoJsonConversions}

import scala.jdk.CollectionConverters._


object ExternalReferencesTypeRepository {
  def getReferenceType(typeName: String) = {
    Dynamo.referencesTypeTable.getItemByStringKey("typeName", typeName).map(ExternalReferenceType.fromItem)
  }

  def updateReferenceType(referenceTypeJson: JsValue) = {
    try {
      val ref = ExternalReferenceType.fromJson(referenceTypeJson)
      Dynamo.referencesTypeTable.putItem(DynamoJsonConversions.jsonToDocument(referenceTypeJson))
      Some(ref)
    } catch {
      case e: Error => None
    }
  }

  def loadAllReferenceTypes: List[ExternalReferenceType] = Dynamo.referencesTypeTable.scan().map(ExternalReferenceType.fromItem).toList

}
