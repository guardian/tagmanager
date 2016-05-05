package model

import play.api.libs.json._
import org.cvogt.play.json.Jsonx
import org.cvogt.play.json.implicits.optionWithNull
import com.gu.tagmanagement.{PodcastMetadata => ThriftPodcastMetadata, PodcastCategory => ThriftPodcastCategory}

case class PodcastMetadata( linkUrl: String,
                            copyrightText: Option[String] = None,
                            authorText: Option[String] = None,
                            iTunesUrl: Option[String] = None,
                            iTunesBlock: Boolean = false,
                            clean: Boolean = false,
                            explicit: Boolean = false,
                            image: Option[Image] = None,
                            categories: Option[PodcastCategory] = None
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
    categories =        categories.map((cat) => List(cat.asThrift))
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
    <categories>{this.categories.map(_.asExportedXml).getOrElse("")}</categories>
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
      categories =        thriftPodcastMetadata.categories.map(x => PodcastCategory(x.head))
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
