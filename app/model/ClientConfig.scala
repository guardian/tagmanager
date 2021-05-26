package model

import ai.x.play.json.Encoders.encoder
import ai.x.play.json.Jsonx

case class ClientConfig(username: String,
                        capiUrl: String,
                        capiPreviewUrl: String,
                        capiKey: String,
                        tagTypes: List[String],
                        permittedTagTypes: List[String],
                        permissions: Map[String, Boolean],
                        reauthUrl: String,
                        tagSearchPageSize: Int
                       )

object ClientConfig {
  implicit val clientConfigFormat = Jsonx.formatCaseClass[ClientConfig]
}
