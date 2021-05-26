package model

import ai.x.play.json.Jsonx
import ai.x.play.json.Encoders.encoder

case class TagSearchResult(tags: List[Tag], count: Int)

object TagSearchResult {
  implicit val tagSearchResultFormat = Jsonx.formatCaseClassUseDefaults[TagSearchResult]
}
