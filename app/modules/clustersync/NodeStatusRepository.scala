package modules.clustersync

import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.spec.{PutItemSpec, UpdateItemSpec}
import com.amazonaws.services.dynamodbv2.document.utils.{NameMap, ValueMap}
import com.amazonaws.services.dynamodbv2.model.{ConditionalCheckFailedException, ReturnValue}
import org.joda.time.DateTime
import play.api.libs.json.JodaWrites._
import play.api.libs.json.JodaReads._
import play.api.Logging
import services.Dynamo

import scala.collection.JavaConversions._


case class HeartbeatException(message: String) extends RuntimeException(message)

object NodeStatusRepository extends Logging {

  def register(): NodeStatus = {
    val currentState = getCurrentState

    val firstAvailable = currentState.find{ n =>
      n.heartbeat.map(_.isBefore(new DateTime().minusMinutes(5))).getOrElse(true)
    }

    firstAvailable match {
      case Some(ns) => {
        logger.info(s"registering as available node id ${ns.nodeId}")
        heartbeat(ns)
      }
      case None => {
        logger.info(s"no available node ids, creating new ")
        registerNewNode(generateNextNodeId(currentState))
      }
    }
  }

  private def generateNextNodeId(currentNodes: List[NodeStatus]) = {
    if(currentNodes.isEmpty) {
      1l
    } else {
      currentNodes.map(_.nodeId).max + 1
    }
  }

  def heartbeat(nodeStatus: NodeStatus): NodeStatus = {

    val heartbeatMillis = System.currentTimeMillis

    val updateParams = new ValueMap().withLong(":next", heartbeatMillis)

    nodeStatus.heartbeat match {
      case Some(dt) => updateParams.withLong(":current", dt.getMillis)
      case None => updateParams.withNull(":current")
    }

    val heartbeatUpdate = new UpdateItemSpec()
      .withPrimaryKey("nodeId", nodeStatus.nodeId)
      .withUpdateExpression("set #h = :next")
      .withConditionExpression("#h = :current")
      .withNameMap(new NameMap().`with`("#h", "heartbeat"))
      .withValueMap(updateParams)
      .withReturnValues(ReturnValue.ALL_NEW)

    try {
      NodeStatus.fromItem( Dynamo.clusterStatusTable.updateItem(heartbeatUpdate).getItem )
    } catch {
      case e: ConditionalCheckFailedException => {
        logger.warn("heartbeat failed", e)
        throw HeartbeatException("heartbeat failed")
      }
    }
  }

  private def registerNewNode(nodeId: Long): NodeStatus = {

    val item = new Item().withLong("nodeId", nodeId).withLong("heartbeat", System.currentTimeMillis)
    val nodeRegistrationPut = new PutItemSpec()
      .withItem(item)
      .withConditionExpression("attribute_not_exists(nodeId)")

    try {
      Dynamo.clusterStatusTable.putItem(nodeRegistrationPut)
      NodeStatus.fromItem(item)
    } catch {
      case e: ConditionalCheckFailedException => {
        logger.warn("node registration failed", e)
        throw HeartbeatException("registration failed")
      }
    }
  }

  def deregister(nodeStatus: NodeStatus) {

    logger.info(s"deregistering as node ${nodeStatus.nodeId}")

    val heartbeatUpdate = new UpdateItemSpec()
      .withPrimaryKey("nodeId", nodeStatus.nodeId)
      .withUpdateExpression("set #h = :next")
      .withConditionExpression("#h = :current")
      .withNameMap(new NameMap().`with`("#h", "heartbeat"))
      .withValueMap(
        new ValueMap().withNull(":next").withLong(":current", nodeStatus.heartbeat.get.getMillis)
      )
      .withReturnValues(ReturnValue.ALL_NEW)

    try {
      Dynamo.clusterStatusTable.updateItem(heartbeatUpdate)
    } catch {
      case e: ConditionalCheckFailedException => {
        logger.warn("heartbeat failed", e)
        throw HeartbeatException("heartbeat failed")
      }
    }
  }

  def getCurrentState = Dynamo.clusterStatusTable.scan().map(NodeStatus.fromItem).toList

}

case class NodeStatus(nodeId: Long, heartbeat: Option[DateTime])

object NodeStatus {
  def fromItem(item: Item) = {
    val heartbeatLong = if(item.isNull("heartbeat")) None else Option(item.getLong("heartbeat"))
    NodeStatus(
      nodeId = item.getLong("nodeId"),
      heartbeat = heartbeatLong.map(new DateTime(_))
    )
  }
}
