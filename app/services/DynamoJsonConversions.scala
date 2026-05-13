package services

import play.api.libs.json._
import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

import scala.jdk.CollectionConverters._

/**
 * Helper object for converting between Play JSON and DynamoDB EnhancedDocument.
 * This preserves the existing JSON-based serialization pattern used in the codebase.
 */
object DynamoJsonConversions {

  /**
   * Convert a Play JSON JsValue to a DynamoDB EnhancedDocument
   */
  def jsonToDocument(json: JsValue): EnhancedDocument = {
    EnhancedDocument.fromJson(Json.stringify(json))
  }
}