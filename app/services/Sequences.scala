package services

import com.amazonaws.services.dynamodbv2.document.{Table, Item, AttributeUpdate}
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec
import com.amazonaws.services.dynamodbv2.model.ReturnValue


object Sequences {

  val tagId = new DynamoSequence(Dynamo.sequenceTable, "tagId")
  val sectionId = new DynamoSequence(Dynamo.sequenceTable, "sectionId")

}

class DynamoSequence(sequenceTable: Table, sequenceName: String) {
  def getNextId: Long = {
    val incResult = sequenceTable.updateItem(
      new UpdateItemSpec().withPrimaryKey("sequenceName", sequenceName)
        .withAttributeUpdate(new AttributeUpdate("value").addNumeric(1))
        .withReturnValues(ReturnValue.ALL_NEW)
    )
    incResult.getItem.getLong("value")
  }

  // debug methods
  def getCurrentId: Long = {
    Dynamo.sequenceTable.getItem("sequenceName", sequenceName).getLong("value")
  }

  def setCurrentId(v: Long) {
    Dynamo.sequenceTable.putItem(new Item().withString("sequenceName", sequenceName).withLong("value", v))
  }
}
