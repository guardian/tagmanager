package model

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class BatchTagOperationTest extends AnyFlatSpec with Matchers {
  "AddToBottom operation" should "parse correctly" in {
    BatchTagOperation.AddToBottom.entryName should be ("add-to-bottom")
  }
}