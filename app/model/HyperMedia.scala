package model
import play.api.libs.json._

case class LinkEntity(rel: String, href: String)
object LinkEntity { implicit val jsonWrites = Json.writes[LinkEntity] }

case class EmbeddedEntity(uri: String,
                             data: Option[Tag] = None,
                             links: Option[List[LinkEntity]] = None) {

  def addLink(rel: String, href: String) = copy(links = Some(LinkEntity(rel, href) :: (links getOrElse Nil)))
}
object EmbeddedEntity { implicit val jsonWrites = Json.writes[EmbeddedEntity] }

case class EntityResponse(data: Tag, links: Option[List[LinkEntity]] = None) {
  def addLink(rel: String, href: String) = copy(links = Some(LinkEntity(rel, href) :: (links getOrElse Nil)))
}
object EntityResponse { implicit val jsonWrites = Json.writes[EntityResponse] }

case class CollectionResponse(
  offset: Int,
  limit: Int,
  total: Option[Int],
  data: List[EmbeddedEntity],
  links: Option[List[LinkEntity]] = None
) {
  def addLink(rel: String, href: String) = copy(links = Some(LinkEntity(rel, href) :: (links getOrElse Nil)))
}
object CollectionResponse { implicit val jsonWrites = Json.writes[CollectionResponse] }


case class EmptyResponse(links: Option[List[LinkEntity]] = None) {
  def addLink(rel: String, href: String) = copy(links = Some(LinkEntity(rel, href) :: (links getOrElse Nil)))
}
object EmptyResponse { implicit val jsonWrites = Json.writes[EmptyResponse] }
