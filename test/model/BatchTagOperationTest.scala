package model

import org.scalatest._

class BatchTagOperationTest extends FlatSpec with Matchers {
  "AddToBottom operation" should "parse correctly" in {
    BatchTagOperation.AddToBottom.entryName should be ("add-to-bottom")
  }
}