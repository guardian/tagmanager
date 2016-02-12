package repositories

import java.util.concurrent.atomic.AtomicReference

import com.amazonaws.services.dynamodbv2.document.{Item, ScanFilter}
import model.Tag
import play.api.Logger
import play.api.libs.json.JsValue
import services.Dynamo
import scala.collection.JavaConversions._


object TagRepository {
  def getTag(id: Long) = {
    Option(Dynamo.tagTable.getItem("id", id)).map(Tag.fromItem)
  }

  def upsertTag(tag: Tag) = {
    try {
      Dynamo.tagTable.putItem(tag.toItem)
      Some(tag)
    } catch {
      case e: Error => None
    }
  }

  def deleteTag(tagId: Long): Unit = {
    Dynamo.tagTable.deleteItem("id", tagId)
  }

  def scanSearch(criteria: TagSearchCriteria) = {
    Dynamo.tagTable.scan(criteria.asFilters: _*).map { item =>
      item.getString("internalName")
    }
  }

  def loadAllTags = Dynamo.tagTable.scan().map(Tag.fromItem)

  def allTagIter = Dynamo.tagTable.scan()
}

case class TagSearchCriteria(
  q: Option[String] = None,
  types: Option[List[String]] = None,
  internalName: Option[String] = None,
  externalName: Option[String] = None,
  referenceType: Option[String] = None,
  referenceToken: Option[String] = None,
  searchField: Option[String] = None
) {

  type TagFilter = (List[Tag]) => List[Tag]

  val filters: List[TagFilter] =  Nil ++
    internalName.map(v => internalNameFilter(v.toLowerCase) _) ++
    externalName.map(v => externalNameFilter(v.toLowerCase) _) ++
    types.map(v => typeFilter(v.map(_.toLowerCase)) _) ++
    q.map(v => queryFilter(v.toLowerCase) _) ++
    referenceType.map(v => referenceTypeFilter(v.toLowerCase) _) ++
    referenceToken.map(v => referenceTokenFilter(v.toLowerCase) _)

  def execute(tags: List[Tag]): List[Tag] = {
    filters.foldLeft(tags){ case(ts, filter) => filter(ts) }
  }

  private def getSearchField(t: Tag ) = {
    searchField.getOrElse("internalName") match {
      case "externalName" => t.externalName.toLowerCase
      case "id" => t.id.toString
      case "type" => t.`type`.toLowerCase
      case "path" => t.path.toLowerCase
      case _ => t.internalName.toLowerCase
    }
  }

  private def queryFilter(q: String)(tags: List[Tag]) = {
    if (q.contains("*")) {
      wildcardSearch(q)(tags)
    } else {
      prefixSearch(q)(tags)
    }
  }

  private def prefixSearch(q: String)(tags: List[Tag]) = {
    tags.filter { t => getSearchField(t).startsWith(q) }
  }

  val regexEscapeChars = List("\\", "(", ")", ".", "?")

  private def generateSearchRegex(q: String) = {
    val escapedQuery = regexEscapeChars.fold(q){case (search, c) => search.replace(c, "\\" + c)}

    (escapedQuery.replace("*", ".*") + ".*").r
  }

  private def wildcardSearch(q: String)(tags: List[Tag]) = {
    val MatchesQuery = generateSearchRegex(q)

    tags.filter { t =>
      getSearchField(t) match {
        case MatchesQuery() => true
        case _ => false
      }
    }
  }

  private def typeFilter(types: List[String])(tags: List[Tag]) = tags.filter { t => types.contains(t.`type`.toLowerCase) }

  private def internalNameFilter(n: String)(tags: List[Tag]) = tags.filter{ t => t.internalName.toLowerCase == n }
  private def externalNameFilter(n: String)(tags: List[Tag]) = tags.filter{ t => t.externalName.toLowerCase == n }

  private def referenceTypeFilter(n: String)(tags: List[Tag]) = tags.filter{ t => t.externalReferences.exists(_.`type`.toLowerCase == n) }
  private def referenceTokenFilter(n: String)(tags: List[Tag]) = tags.filter{ t => t.externalReferences.exists(_.value.toLowerCase == n) }


  def asFilters = {
    Seq() ++
      q.map{query => new ScanFilter("internalName").beginsWith(query)} ++
      types.map{ts => new ScanFilter("type").in(ts: _*)}
  }
}

object TagLookupCache {

  val allTags = new AtomicReference[List[Tag]](Nil)

  def refresh = allTags.set(TagRepository.loadAllTags.toList.sortBy(_.internalName))

  def insertTag(tag: Tag): Unit = {
    val currentTags = allTags.get()
    val newTags = (tag :: currentTags.filterNot(_.id == tag.id)).sortBy(_.internalName)

    if (!allTags.compareAndSet(currentTags, newTags))
      Logger.warn("failed to update cache")
  }

  def removeTag(tagId: Long): Unit = {
    val currentTags = allTags.get()
    val newTags = currentTags.filterNot(_.id == tagId).sortBy(_.internalName)

    if (!allTags.compareAndSet(currentTags, newTags))
      Logger.warn("failed to update cache")
  }

  def search(tagSearchCriteria: TagSearchCriteria) = {
    tagSearchCriteria.execute(allTags.get())
  }

  def getTag(tagId: Long): Option[Tag] = {
    allTags.get().find(_.id == tagId)
  }
}
