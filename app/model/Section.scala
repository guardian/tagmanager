package model

import com.amazonaws.services.dynamodbv2.document.Item
import helpers.XmlHelpers._
import ai.x.play.json.Jsonx
import ai.x.play.json.Encoders.encoder
import ai.x.play.json.implicits.optionWithNull
import play.api.Logging
import play.api.libs.json.{JsValue, Json, JsPath, Format}
import com.gu.tagmanagement.{Section => ThriftSection}
import repositories.SponsorshipRepository

import scala.util.control.NonFatal
import scala.xml.Node
import com.madgag.scala.collection.decorators._

case class Section(
                    id: Long,
                    sectionTagId: Long,
                    name: String,
                    path: String,
                    wordsForUrl: String,
                    pageId: Long,
                    editions: Map[String, EditionalisedPage] = Map(),
                    discriminator: Option[String] = None,
                    isMicrosite: Boolean,
                    activeSponsorships: List[Long] = Nil
                    ) {

  def toItem = Item.fromJSON(Json.toJson(this).toString())

  def asThrift = ThriftSection(
    id            = id,
    sectionTagId  = sectionTagId,
    name          = name,
    path          = path,
    wordsForUrl   = wordsForUrl,
    pageId        = pageId,
    editions      = editions.mapV(_.asThift),
    discriminator = discriminator,
    isMicrosite   = isMicrosite,
    activeSponsorships = if (activeSponsorships.isEmpty) None else Some(activeSponsorships.flatMap {sid =>
      SponsorshipRepository.getSponsorship(sid).map(_.asThrift)
    })
  )

  // in this limited format for inCopy to consume
  def asExportedXml = {

    val el = createElem("section")
    val id = createAttribute("id", Some(this.id))
    val name = createAttribute("name", Some(this.name))
    val launched = createAttribute("launched", Some(true))
    val microsite = createAttribute("microsite", Some(this.isMicrosite))

    el % id % name % launched % microsite

  }
}

object Section extends Logging {

  implicit val sectionFormat: Format[Section] = Jsonx.formatCaseClassUseDefaults[Section]

  def fromItem(item: Item) = try{
    Json.parse(item.toJSON).as[Section]
  } catch {
    case NonFatal(e) => logger.error(s"failed to load section ${item.toJSON}", e); throw e

  }

  def fromJson(json: JsValue) = json.as[Section]

  def apply(thriftSection: ThriftSection): Section =
    Section(
      id            = thriftSection.id,
      sectionTagId  = thriftSection.sectionTagId,
      name          = thriftSection.name,
      path          = thriftSection.path,
      wordsForUrl   = thriftSection.wordsForUrl,
      pageId        = thriftSection.pageId,
      editions      = thriftSection.editions.mapV(EditionalisedPage(_)),
      discriminator = thriftSection.discriminator,
      isMicrosite   = thriftSection.isMicrosite,
      activeSponsorships = thriftSection.activeSponsorships.map(_.map(_.id).toList).getOrElse(Nil)
    )

}
