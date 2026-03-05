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

  /**
   * Convert a DynamoDB EnhancedDocument to Play JSON JsValue
   */
  def documentToJson(doc: EnhancedDocument): JsValue = {
    Json.parse(doc.toJson())
  }

  /**
   * Convert a Play JSON JsValue to a DynamoDB AttributeValue
   */
  def jsonToAttributeValue(json: JsValue): AttributeValue = json match {
    case JsNull => AttributeValue.builder().nul(true).build()
    case JsBoolean(b) => AttributeValue.builder().bool(b).build()
    case JsNumber(n) => AttributeValue.builder().n(n.toString()).build()
    case JsString(s) => AttributeValue.builder().s(s).build()
    case JsArray(arr) =>
      if (arr.isEmpty) {
        AttributeValue.builder().l(java.util.Collections.emptyList[AttributeValue]()).build()
      } else {
        AttributeValue.builder().l(arr.map(jsonToAttributeValue).asJava).build()
      }
    case JsObject(obj) =>
      if (obj.isEmpty) {
        AttributeValue.builder().m(java.util.Collections.emptyMap[String, AttributeValue]()).build()
      } else {
        AttributeValue.builder().m(obj.map { case (k, v) => k -> jsonToAttributeValue(v) }.asJava).build()
      }
  }

  /**
   * Convert a DynamoDB AttributeValue to a Play JSON JsValue
   */
  def attributeValueToJson(av: AttributeValue): JsValue = {
    if (av.nul() != null && av.nul()) {
      JsNull
    } else if (av.bool() != null) {
      JsBoolean(av.bool())
    } else if (av.n() != null) {
      val num = av.n()
      if (num.contains(".")) JsNumber(BigDecimal(num))
      else JsNumber(BigDecimal(num.toLong))
    } else if (av.s() != null) {
      JsString(av.s())
    } else if (av.hasL) {
      JsArray(av.l().asScala.map(attributeValueToJson).toSeq)
    } else if (av.hasM) {
      JsObject(av.m().asScala.map { case (k, v) => k -> attributeValueToJson(v) }.toMap)
    } else if (av.hasSs) {
      JsArray(av.ss().asScala.map(JsString(_)).toSeq)
    } else if (av.hasNs) {
      JsArray(av.ns().asScala.map(n => JsNumber(BigDecimal(n))).toSeq)
    } else {
      JsNull
    }
  }

  /**
   * Convert a JSON string to a DynamoDB item (Map of AttributeValues)
   */
  def jsonStringToItem(jsonString: String): java.util.Map[String, AttributeValue] = {
    val json = Json.parse(jsonString).as[JsObject]
    json.fields.map { case (k, v) => k -> jsonToAttributeValue(v) }.toMap.asJava
  }

  /**
   * Convert a DynamoDB item to a JSON string
   */
  def itemToJsonString(item: java.util.Map[String, AttributeValue]): String = {
    val json = JsObject(item.asScala.map { case (k, v) => k -> attributeValueToJson(v) }.toMap)
    Json.stringify(json)
  }

  /**
   * Convert a JsValue (typically JsObject) to a DynamoDB item
   */
  def jsonToItem(json: JsValue): java.util.Map[String, AttributeValue] = {
    json match {
      case obj: JsObject => obj.fields.map { case (k, v) => k -> jsonToAttributeValue(v) }.toMap.asJava
      case _ => throw new IllegalArgumentException("Expected JsObject for DynamoDB item")
    }
  }

  /**
   * Convert a DynamoDB item to a JsValue
   */
  def itemToJson(item: java.util.Map[String, AttributeValue]): JsValue = {
    JsObject(item.asScala.map { case (k, v) => k -> attributeValueToJson(v) }.toMap)
  }
}



