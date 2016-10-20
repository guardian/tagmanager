package model
import play.api.libs.json._
import services.Config.conf


case class LinkEntity(rel: String, href: String)
object LinkEntity { implicit val jsonWrites = Json.writes[LinkEntity] }

case class EmbeddedEntity[T](uri: String,
                             data: Option[T] = None,
                             links: Option[List[LinkEntity]] = None) {
  def addLink(rel: String, href: String) = copy(links = Some(LinkEntity(rel, href) :: (links getOrElse Nil)))
}
object EmbeddedEntity {
  implicit def embeddedEntityWrites[T](implicit fmt: Writes[T]): Writes[EmbeddedEntity[T]] = new Writes[EmbeddedEntity[T]] {
    def writes(ts: EmbeddedEntity[T]) = JsObject(Seq(
      "uri" -> JsString(ts.uri),
      "data" -> Json.toJson(ts.data),
      "links" -> JsArray(ts.links.map(_.map(x => Json.toJson(x))).getOrElse(Nil))
    ))
  }

}

case class EntityResponse[T](data: T, links: Option[List[LinkEntity]] = None) {
  def addLink(rel: String, href: String) = copy(links = Some(LinkEntity(rel, href) :: (links getOrElse Nil)))
}
object EntityResponse {
  implicit def entityResponseWrites[T](implicit fmt: Writes[T]): Writes[EntityResponse[T]] = new Writes[EntityResponse[T]] {
    def writes(er: EntityResponse[T]) = JsObject(Seq(
      "data" -> Json.toJson(er.data),
      "links" -> JsArray(er.links.map(_.map(x => Json.toJson(x))).getOrElse(Nil))
    ))
  }
}

case class CollectionResponse[T](
  offset: Int,
  limit: Int,
  total: Option[Int],
  data: List[EmbeddedEntity[T]],
  links: Option[List[LinkEntity]] = None
) {
  def addLink(rel: String, href: String) = copy(links = Some(LinkEntity(rel, href) :: (links getOrElse Nil)))
}
object CollectionResponse {
  implicit def collectionResponseWrites[T](implicit fmt: Writes[T]): Writes[CollectionResponse[T]] = new Writes[CollectionResponse[T]] {
    def writes(cr: CollectionResponse[T]) = JsObject(Seq(
      "offset" -> JsNumber(cr.offset),
      "limit" -> JsNumber(cr.limit),
      "total" -> JsNumber(cr.total.getOrElse(0).toInt),
      "data" -> JsArray(cr.data.map(x => Json.toJson(x))),
      "links" -> JsArray(cr.links.map(_.map(x => Json.toJson(x))).getOrElse(Nil))
    ))
  }

}

case class EmptyResponse(links: Option[List[LinkEntity]] = None) {
  def addLink(rel: String, href: String) = copy(links = Some(LinkEntity(rel, href) :: (links getOrElse Nil)))
}
object EmptyResponse { implicit val jsonWrites = Json.writes[EmptyResponse] }

object HyperMediaHelpers {
  def tagUri(id: Long): String = s"https://tagmanager.${conf.pandaDomain}/hyper/tags/${id}"
  def sponsorshipUri(id: Long): String = s"https://tagmanager.${conf.pandaDomain}/hyper/sponsorships/${id}"
  def fullUri(path: String): String = s"https://tagmanager.${conf.pandaDomain}${path}"
}
