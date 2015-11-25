package model

import play.api.libs.json._
import play.api.libs.functional.syntax._
import com.gu.tagmanagement.{PodcastMetadata => ThriftPodcastMetadata}

case class PodcastMetadata( linkUrl: String,
                            copyrightText: String,
                            authorText: String,
                            iTunesUrl: String,
                            iTunesBlock: Boolean,
                            clean: Boolean,
                            explicit: Boolean
) {

  def asThrift = ThriftPodcastMetadata(
    linkUrl =           linkUrl,
    copyrightText =     copyrightText,
    authorText =        authorText,
    iTunesUrl =         iTunesUrl,
    iTunesBlock =       iTunesBlock,
    clean =             clean,
    explicit =          explicit
  )
}

object PodcastMetadata {

  implicit val podcastMetadataFormat: Format[PodcastMetadata] = (
      (JsPath \ "linkUrl").format[String] and
        (JsPath \ "copyrightText").format[String] and
        (JsPath \ "authorText").format[String] and
        (JsPath \ "iTunesUrl").format[String] and
        (JsPath \ "iTunesBlock").format[Boolean] and
        (JsPath \ "clean").format[Boolean] and
        (JsPath \ "explicit").format[Boolean]
    )(PodcastMetadata.apply, unlift(PodcastMetadata.unapply))

  def apply(thriftPodcastMetadata: ThriftPodcastMetadata): PodcastMetadata =
    PodcastMetadata(
      linkUrl =           thriftPodcastMetadata.linkUrl,
      copyrightText =     thriftPodcastMetadata.copyrightText,
      authorText =        thriftPodcastMetadata.authorText,
      iTunesUrl =         thriftPodcastMetadata.iTunesUrl,
      iTunesBlock =       thriftPodcastMetadata.iTunesBlock,
      clean =             thriftPodcastMetadata.clean,
      explicit =          thriftPodcastMetadata.explicit
    )
}

