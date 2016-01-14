package model

import play.api.libs.json._
import play.api.libs.functional.syntax._
import com.gu.tagmanagement.{PodcastMetadata => ThriftPodcastMetadata}

case class PodcastMetadata( linkUrl: String,
                            copyrightText: Option[String],
                            authorText: Option[String],
                            iTunesUrl: Option[String],
                            iTunesBlock: Boolean,
                            clean: Boolean,
                            explicit: Boolean,
                            image: Option[Image] = None
) {

  def asThrift = ThriftPodcastMetadata(
    linkUrl =           linkUrl,
    copyrightText =     copyrightText,
    authorText =        authorText,
    iTunesUrl =         iTunesUrl,
    iTunesBlock =       iTunesBlock,
    clean =             clean,
    explicit =          explicit,
    image =             image.map(_.asThrift)
  )

  def asExportedXml = {
    <linkUrl>{this.linkUrl}</linkUrl>
    <copyrightText>{this.copyrightText.getOrElse("")}</copyrightText>
    <authorText>{this.authorText.getOrElse("")}</authorText>
    <iTunesUrl>{this.iTunesUrl.getOrElse("")}</iTunesUrl>
    <iTunesBlock>{this.iTunesBlock}</iTunesBlock>
    <clean>{this.clean}</clean>
    <explicit>{this.explicit}</explicit>
    <image>{this.image.map(x => x.asExportedXml).getOrElse("")}</image>
  }
}

object PodcastMetadata {

  implicit val podcastMetadataFormat: Format[PodcastMetadata] = (
      (JsPath \ "linkUrl").format[String] and
        (JsPath \ "copyrightText").formatNullable[String] and
        (JsPath \ "authorText").formatNullable[String] and
        (JsPath \ "iTunesUrl").formatNullable[String] and
        (JsPath \ "iTunesBlock").format[Boolean] and
        (JsPath \ "clean").format[Boolean] and
        (JsPath \ "explicit").format[Boolean] and
        (JsPath \ "image").formatNullable[Image]
    )(PodcastMetadata.apply, unlift(PodcastMetadata.unapply))

  def apply(thriftPodcastMetadata: ThriftPodcastMetadata): PodcastMetadata =
    PodcastMetadata(
      linkUrl =           thriftPodcastMetadata.linkUrl,
      copyrightText =     thriftPodcastMetadata.copyrightText,
      authorText =        thriftPodcastMetadata.authorText,
      iTunesUrl =         thriftPodcastMetadata.iTunesUrl,
      iTunesBlock =       thriftPodcastMetadata.iTunesBlock,
      clean =             thriftPodcastMetadata.clean,
      explicit =          thriftPodcastMetadata.explicit,
      image =             thriftPodcastMetadata.image.map(Image(_))
    )
}
