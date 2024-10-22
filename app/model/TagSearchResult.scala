package model

import ai.x.play.json.Jsonx
import ai.x.play.json.Encoders.encoder
import play.api.libs.json.OFormat

case class TagSearchResult(tags: List[Tag], count: Int)

object TagSearchResult {
  implicit val tagSearchResultFormat: OFormat[TagSearchResult] = Jsonx.formatCaseClassUseDefaults[TagSearchResult]
}
