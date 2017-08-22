package model

import org.cvogt.play.json.Jsonx

case class TagSearchResult(tags: List[Tag], count: Int)

object TagSearchResult {
  implicit val tagSearchResultFormat = Jsonx.formatCaseClassUseDefaults[TagSearchResult]
}