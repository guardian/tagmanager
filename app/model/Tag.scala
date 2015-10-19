package model

case class Tag(
  id: Long,
  path: String,
  `type`: String,
  internalName: String,
  externalName: String,
  slug: String,
  hidden: Boolean = false,
  comparableValue: String,
  section: Long,
  description: Option[String] = None,
  parents: Set[Long] = Set(),
  references: List[Reference] = Nil
)

