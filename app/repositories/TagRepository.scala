package repositories

import com.amazonaws.services.dynamodbv2.document.{ScanFilter, Item}
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec
import model.Tag
import services.Dynamo
import scala.collection.JavaConversions._


object TagRepository {
  def getTag(id: Long) = {
    Option(Dynamo.tagTable.getItem("id", id)).map(Tag.fromItem)
  }


  def search(criteria: TagSearchCriteria) = {
    Dynamo.tagTable.scan(criteria.asFilters: _*).map { item =>
      item.getString("internalName")
    }
  }

}

case class TagSearchCriteria(
  q: Option[String] = None,
  types: Option[List[String]] = None,
  internalName: Option[String] = None,
  externalName: Option[String] = None,
  referenceType: Option[String] = None,
  referenceToken: Option[String] = None
) {

  def asFilters = {
    Seq() ++
      q.map{query => new ScanFilter("internalName").beginsWith(query)} ++
      types.map{ts => new ScanFilter("type").in(ts: _*)}
  }
}
