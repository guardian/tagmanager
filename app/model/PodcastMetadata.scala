package model

import play.api.libs.json._
import ai.x.play.json.Jsonx
import ai.x.play.json.Encoders.encoder
import ai.x.play.json.implicits.optionWithNull
import com.gu.tagmanagement.{PodcastMetadata => ThriftPodcastMetadata, PodcastCategory => ThriftPodcastCategory}

case class PodcastMetadata( linkUrl: String,
                            copyrightText: Option[String] = None,
                            authorText: Option[String] = None,
                            iTunesUrl: Option[String] = None,
                            iTunesBlock: Boolean = false,
                            clean: Boolean = false,
                            explicit: Boolean = false,
                            image: Option[Image] = None,
                            categories: Option[PodcastCategory] = None,
                            podcastType: Option[String] = None,
                            googlePodcastsUrl: Option[String] = None,
                            spotifyUrl: Option[String] = None,
                            acastId: Option[String] = None,
                            pocketCastsUrl: Option[String] = None
) {

  def asThrift = ThriftPodcastMetadata(
    linkUrl =           linkUrl,
    copyrightText =     copyrightText,
    authorText =        authorText,
    iTunesUrl =         iTunesUrl,
    iTunesBlock =       iTunesBlock,
    clean =             clean,
    explicit =          explicit,
    image =             image.map(_.asThrift),
    categories =        categories.map((cat) => List(cat.asThrift)),
    podcastType =       podcastType,
    googlePodcastsUrl = googlePodcastsUrl,
    spotifyUrl =        spotifyUrl,
    acastId =           acastId,
    pocketCastsUrl =    pocketCastsUrl
  )

  def asExportedXml = {
    <linkUrl>{this.linkUrl}</linkUrl>
    <copyrightText>{this.copyrightText.getOrElse("")}</copyrightText>
    <authorText>{this.authorText.getOrElse("")}</authorText>
    <iTunesUrl>{this.iTunesUrl.getOrElse("")}</iTunesUrl>
    <iTunesBlock>{this.iTunesBlock}</iTunesBlock>
    <GooglePodcastsUrl>{this.googlePodcastsUrl.getOrElse("")}</GooglePodcastsUrl>
    <SpotifyUrl>{this.spotifyUrl.getOrElse("")}</SpotifyUrl>
    <AcastId>{this.acastId.getOrElse("")}</AcastId>
    <PocketCastsUrl>{this.pocketCastsUrl.getOrElse("")}</PocketCastsUrl>
    <clean>{this.clean}</clean>
    <explicit>{this.explicit}</explicit>
    <image>{this.image.map(x => x.asExportedXml).getOrElse("")}</image>
    <categories>{this.categories.map(_.asExportedXml).getOrElse("")}</categories>
    <type>{this.podcastType.getOrElse("")}</type>
  }
}

object PodcastMetadata {

  implicit val podcastMetadataFormat: Format[PodcastMetadata] = Jsonx.formatCaseClassUseDefaults[PodcastMetadata]

  def apply(thriftPodcastMetadata: ThriftPodcastMetadata): PodcastMetadata =
    PodcastMetadata(
      linkUrl =           thriftPodcastMetadata.linkUrl,
      copyrightText =     thriftPodcastMetadata.copyrightText,
      authorText =        thriftPodcastMetadata.authorText,
      iTunesUrl =         thriftPodcastMetadata.iTunesUrl,
      iTunesBlock =       thriftPodcastMetadata.iTunesBlock,
      clean =             thriftPodcastMetadata.clean,
      explicit =          thriftPodcastMetadata.explicit,
      image =             thriftPodcastMetadata.image.map(Image(_)),
      categories =        thriftPodcastMetadata.categories.map(x => PodcastCategory(x.head)),
      podcastType =       thriftPodcastMetadata.podcastType,
      googlePodcastsUrl = thriftPodcastMetadata.googlePodcastsUrl,
      spotifyUrl =        thriftPodcastMetadata.spotifyUrl,
      acastId =           thriftPodcastMetadata.acastId,
      pocketCastsUrl =    thriftPodcastMetadata.pocketCastsUrl
    )
}

case class PodcastCategory(
                            main: String,
                            sub: Option[String]
                          ) {
  def asThrift = ThriftPodcastCategory(
    main = main,
    sub = sub
  )

  def asExportedXml = {
    <main>{this.main}</main>
    <sub>{this.sub.getOrElse("")}</sub>
  }
}

object PodcastCategory {
  implicit val podcastCategoryFormat: Format[PodcastCategory] = Jsonx.formatCaseClassUseDefaults[PodcastCategory]

  def apply(thriftPodcastCategory: ThriftPodcastCategory): PodcastCategory =
    PodcastCategory(
      main = thriftPodcastCategory.main,
      sub = thriftPodcastCategory.sub
    )
}
