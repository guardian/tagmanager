package model

import ai.x.play.json.Encoders.encoder
import ai.x.play.json.Jsonx
import play.api.libs.json.OFormat

case class ClientConfig(username: String,
                        capiUrl: String,
                        capiPreviewUrl: String,
                        capiKey: String,
                        tagTypes: List[String],
                        permittedTagTypes: List[String],
                        permissions: Map[String, Boolean],
                        reauthUrl: String,
                        tagSearchPageSize: Int,
                        permittedKeywordTypes: List[String],
                       )

object ClientConfig {
  implicit val clientConfigFormat: OFormat[ClientConfig] = Jsonx.formatCaseClass[ClientConfig]
}
