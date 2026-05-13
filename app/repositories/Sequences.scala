package repositories

import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument
import software.amazon.awssdk.services.dynamodb.model._
import services.{Dynamo, DynamoTable, DynamoJsonConversions}

import scala.jdk.CollectionConverters._


object Sequences {

  val tagId = new DynamoSequence(Dynamo.sequenceTable, "tagId")
  val sectionId = new DynamoSequence(Dynamo.sequenceTable, "sectionId")
  val sponsorshipId = new DynamoSequence(Dynamo.sequenceTable, "sponsorshipId")
  val jobId = new DynamoSequence(Dynamo.sequenceTable, "jobId")
  val pillarId = new DynamoSequence(Dynamo.sequenceTable, "pillarId")

}

class DynamoSequence(sequenceTable: DynamoTable, sequenceName: String) {
  def getNextId: Long = {
    val response = sequenceTable.updateItem(
      key = Map("sequenceName" -> AttributeValue.builder().s(sequenceName).build()),
      updateExpression = "ADD #v :inc",
      expressionAttributeNames = Map("#v" -> "value"),
      expressionAttributeValues = Map(":inc" -> AttributeValue.builder().n("1").build())
    )
    response.attributes().get("value").n().toLong
  }

  // debug methods
  def getCurrentId: Long = {
    sequenceTable.getItemByStringKey("sequenceName", sequenceName)
      .map(_.getNumber("value").longValue())
      .getOrElse(0L)
  }

  def setCurrentId(v: Long): Unit = {
    val doc = EnhancedDocument.builder()
      .putString("sequenceName", sequenceName)
      .putNumber("value", v)
      .build()
    sequenceTable.putItem(doc)
  }
}
