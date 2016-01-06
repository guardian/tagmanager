package model

case class EntityResponse[T](data: T, links: Option[List[LinkEntity]] = None) {

  def addLink(rel: String, href: String) = copy(links = Some(LinkEntity(rel, href) :: (links getOrElse Nil)))
}

case class CollectionResponse[T](
  offset: Int,
  limit: Int,
  total: Option[Int],
  data: List[T],
  links: Option[List[LinkEntity]] = None
) {

  def addLink(rel: String, href: String) = copy(links = Some(LinkEntity(rel, href) :: (links getOrElse Nil)))
}

case class EmptyResponse(links: Option[List[LinkEntity]] = None) {

  def addLink(rel: String, href: String) = copy(links = Some(LinkEntity(rel, href) :: (links getOrElse Nil)))
}



case class LinkEntity(rel: String, href: String)

case class EmbeddedEntity[T](uri: String,
                             data: Option[T] = None,
                             links: Option[List[LinkEntity]] = None) {

  def addLink(rel: String, href: String) = copy(links = Some(LinkEntity(rel, href) :: (links getOrElse Nil)))
}

case class EmbeddedCollection[T](uri: String,
                                 data: Option[List[T]] = None,
                                 links: Option[List[LinkEntity]] = None) {

  def addLink(rel: String, href: String) = copy(links = Some(LinkEntity(rel, href) :: (links getOrElse Nil)))
}
