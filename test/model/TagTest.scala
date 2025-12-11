package model

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import utils.TagTestUtils.createTestTag

class TagTest extends AnyFlatSpec with Matchers {
    it should "correctly serialise keywordType field when set to PERSON" in {
        val testTag = createTestTag("testKeywordType", 0, 0)
          .copy(keywordType = Some(model.KeywordType.PERSON))

        val thriftTag = testTag.asThrift

        thriftTag.keywordType shouldEqual Some(com.gu.tagmanagement.KeywordType.Person)
    }

    it should "correctly serialise keywordType field when set to WORK_OF_ART_OR_PRODUCT" in {
        val testTag = createTestTag("testKeywordType", 0, 0)
          .copy(keywordType = Some(model.KeywordType.WORK_OF_ART_OR_PRODUCT))

        val thriftTag = testTag.asThrift

        thriftTag.keywordType shouldEqual Some(com.gu.tagmanagement.KeywordType.WorkOfArtOrProduct)
    }
}
