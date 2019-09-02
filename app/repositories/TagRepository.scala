package repositories

import java.util.concurrent.atomic.AtomicReference

import com.amazonaws.services.dynamodbv2.document.{Item, ScanFilter}
import model.Tag
import org.apache.commons.lang3.StringUtils
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
  searchField: Option[String] = None,
  subType: Option[String] = None,
  hasFields: Option[List[String]] = None
) {

  type TagFilter = (List[Tag]) => List[Tag]

  val filters: List[TagFilter] =  Nil ++
    internalName.map(v => internalNameFilter(v.toLowerCase) _) ++
    externalName.map(v => externalNameFilter(v.toLowerCase) _) ++
    types.map(v => typeFilter(v.map(_.toLowerCase)) _) ++
    q.map(v => queryFilter(v.toLowerCase) _) ++
    referenceType.map(v => referenceTypeFilter(v.toLowerCase) _) ++
    referenceToken.map(v => referenceTokenFilter(v.toLowerCase) _) ++
    subType.map(v => subTypeFilter(v.toLowerCase) _) ++
    hasFields.map(v => hasFieldsFilter(v.map(_.toLowerCase)) _)

  def execute(tags: List[Tag]): List[Tag] = {
    filters.foldLeft(tags){ case(ts, filter) => filter(ts) }
  }

  // Get the field and fold certain special characters into something more likely to occur on a keyboard
  private def normalize(t: String) = StringUtils.stripAccents(
    t
      .toLowerCase
      .replace("–", "-")
      .replace("—", "-")
      .replace("−", "-")
      .replace("“", "\"")
      .replace("”", "\"")
      .replace("‘", "'")
      .replace("’", "'")
      .replace("…", "...")
  )

  private def getSearchField(t: Tag ) = {
    val field = searchField.getOrElse("internalName") match {
      case "externalName" => t.externalName
      case "id" => t.id.toString
      case "type" => t.`type`
      case "path" => t.path
      case _ => t.internalName
    }

    normalize(field)
  }

  private def queryFilter(q: String)(tags: List[Tag]) = {
    val query = normalize(q)

    if (query.contains("*")) {
      wildcardSearch(query)(tags)
    } else {
      prefixSearch(query)(tags)
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

  private def hasFieldsFilter(fields: List[String])(tags: List[Tag]) = tags.filter { t =>
    fields.forall {
        case "contributorinformation.bylineimage" => t.contributorInformation.flatMap(_.bylineImage).isDefined
        case "contributorinformation.largebylineimage" => t.contributorInformation.flatMap(_.largeBylineImage).isDefined
        case unsupported => {
          Logger.warn(s"Attempted to check if tag has field '$unsupported' which is not supported by the hasFieldsFilter")
          true
        }
    }
  }

  private def internalNameFilter(n: String)(tags: List[Tag]) = tags.filter{ t => t.internalName.toLowerCase == n }
  private def externalNameFilter(n: String)(tags: List[Tag]) = tags.filter{ t => t.externalName.toLowerCase == n }

  private def referenceTypeFilter(n: String)(tags: List[Tag]) = tags.filter{ t => t.externalReferences.exists(_.`type`.toLowerCase == n) }
  private def referenceTokenFilter(n: String)(tags: List[Tag]) = tags.filter{ t => t.externalReferences.exists(_.value.toLowerCase == n) }

  private def subTypeFilter(subType: String)(tags: List[Tag]) = tags.filter { t =>
    (
      t.trackingInformation.map(_.trackingType.toLowerCase == subType) orElse
      t.paidContentInformation.map(_.paidContentType.toLowerCase == subType)
    ).getOrElse(false)
  }


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
    while (true) {
      val currentTags = allTags.get()

      currentTags.find(_.id == tag.id).foreach(currentTag => {
        if (tag.updatedAt < currentTag.updatedAt) {
          return
        }
      })

      val newTags = (tag :: currentTags.filterNot(_.id == tag.id)).sortBy(_.internalName)

      if (allTags.compareAndSet(currentTags, newTags)) {
        Logger.info(s"updated TagLookupCache with new tag: {internalName: ${tag.internalName}, id: ${tag.id}")
        return
      }
    }
  }

  def removeTag(tagId: Long): Unit = {
    while (true) {
      val currentTags = allTags.get()
      val newTags = currentTags.filterNot(_.id == tagId).sortBy(_.internalName)

      if (allTags.compareAndSet(currentTags, newTags)) {
        return
      }
    }
  }

  def search(tagSearchCriteria: TagSearchCriteria) = {
    tagSearchCriteria.execute(allTags.get())
  }

  def getTag(tagId: Long): Option[Tag] = {
    allTags.get().find(_.id == tagId)
  }
}
