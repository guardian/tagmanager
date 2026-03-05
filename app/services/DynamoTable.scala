package services

import play.api.libs.json._
import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model._

import scala.jdk.CollectionConverters._

/**
 * A wrapper around DynamoDB SDK v2 that provides a similar interface to SDK v1's Document API.
 * Uses EnhancedDocument for JSON-based serialization.
 */
class DynamoTable(val tableName: String, client: DynamoDbClient) {

  def getItem(keyName: String, keyValue: Long): Option[EnhancedDocument] = {
    val request = GetItemRequest.builder()
      .tableName(tableName)
      .key(Map(keyName -> AttributeValue.builder().n(keyValue.toString).build()).asJava)
      .build()
    val response = client.getItem(request)
    if (response.hasItem && !response.item().isEmpty) {
      Some(EnhancedDocument.fromAttributeValueMap(response.item()))
    } else None
  }

  def getItemConsistent(keyName: String, keyValue: Long): Option[EnhancedDocument] = {
    val request = GetItemRequest.builder()
      .tableName(tableName)
      .key(Map(keyName -> AttributeValue.builder().n(keyValue.toString).build()).asJava)
      .consistentRead(true)
      .build()
    val response = client.getItem(request)
    if (response.hasItem && !response.item().isEmpty) {
      Some(EnhancedDocument.fromAttributeValueMap(response.item()))
    } else None
  }

  def getItemByStringKey(keyName: String, keyValue: String): Option[EnhancedDocument] = {
    val request = GetItemRequest.builder()
      .tableName(tableName)
      .key(Map(keyName -> AttributeValue.builder().s(keyValue).build()).asJava)
      .build()
    val response = client.getItem(request)
    if (response.hasItem && !response.item().isEmpty) {
      Some(EnhancedDocument.fromAttributeValueMap(response.item()))
    } else None
  }

  def putItem(doc: EnhancedDocument): Unit = {
    val request = PutItemRequest.builder()
      .tableName(tableName)
      .item(doc.toMap)
      .build()
    client.putItem(request)
  }

  def deleteItem(keyName: String, keyValue: Long): Unit = {
    val request = DeleteItemRequest.builder()
      .tableName(tableName)
      .key(Map(keyName -> AttributeValue.builder().n(keyValue.toString).build()).asJava)
      .build()
    client.deleteItem(request)
  }

  def deleteItemByStringKey(keyName: String, keyValue: String): Unit = {
    val request = DeleteItemRequest.builder()
      .tableName(tableName)
      .key(Map(keyName -> AttributeValue.builder().s(keyValue).build()).asJava)
      .build()
    client.deleteItem(request)
  }

  def scan(): Iterator[EnhancedDocument] = {
    new DynamoScanIterator(tableName, client, None, None)
  }

  def scanWithFilter(
    filterExpression: String,
    expressionAttributeNames: Map[String, String] = Map.empty,
    expressionAttributeValues: Map[String, AttributeValue] = Map.empty
  ): Iterator[EnhancedDocument] = {
    new DynamoScanIterator(
      tableName, client,
      Some(filterExpression),
      Some((expressionAttributeNames, expressionAttributeValues))
    )
  }

  def updateItem(
    key: Map[String, AttributeValue],
    updateExpression: String,
    expressionAttributeNames: Map[String, String] = Map.empty,
    expressionAttributeValues: Map[String, AttributeValue] = Map.empty,
    conditionExpression: Option[String] = None,
    returnValues: ReturnValue = ReturnValue.ALL_NEW
  ): UpdateItemResponse = {
    val builder = UpdateItemRequest.builder()
      .tableName(tableName)
      .key(key.asJava)
      .updateExpression(updateExpression)
      .returnValues(returnValues)

    if (expressionAttributeNames.nonEmpty)
      builder.expressionAttributeNames(expressionAttributeNames.asJava)
    if (expressionAttributeValues.nonEmpty)
      builder.expressionAttributeValues(expressionAttributeValues.asJava)
    conditionExpression.foreach(builder.conditionExpression)

    client.updateItem(builder.build())
  }
}

class DynamoScanIterator(
  tableName: String,
  client: DynamoDbClient,
  filterExpression: Option[String],
  expressionAttributes: Option[(Map[String, String], Map[String, AttributeValue])]
) extends Iterator[EnhancedDocument] {

  private var items: Iterator[EnhancedDocument] = Iterator.empty
  private var lastKey: java.util.Map[String, AttributeValue] = _
  private var hasMore = true
  private var init = false

  private def fetch(): Unit = {
    val builder = ScanRequest.builder().tableName(tableName)
    if (lastKey != null && !lastKey.isEmpty) builder.exclusiveStartKey(lastKey)

    filterExpression.foreach(builder.filterExpression)
    expressionAttributes.foreach { case (names, values) =>
      if (names.nonEmpty) builder.expressionAttributeNames(names.asJava)
      if (values.nonEmpty) builder.expressionAttributeValues(values.asJava)
    }

    val response = client.scan(builder.build())
    items = response.items().asScala.map(item => EnhancedDocument.fromAttributeValueMap(item)).iterator
    lastKey = response.lastEvaluatedKey()
    hasMore = lastKey != null && !lastKey.isEmpty
  }

  override def hasNext: Boolean = {
    if (!init) { fetch(); init = true }
    if (items.hasNext) true else if (hasMore) { fetch(); items.hasNext } else false
  }

  override def next(): EnhancedDocument = {
    if (!hasNext) throw new NoSuchElementException
    items.next()
  }
}

object DynamoTable {
  def create(client: DynamoDbClient, tableName: String): DynamoTable = {
    new DynamoTable(tableName, client)
  }
}


