package modules.clustersync

import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument
import software.amazon.awssdk.services.dynamodb.model._
import org.joda.time.DateTime
import helpers.JodaDateTimeFormat._
import play.api.Logging
import services.{Dynamo, DynamoJsonConversions}

import scala.jdk.CollectionConverters._


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
      1L
    } else {
      currentNodes.map(_.nodeId).max + 1
    }
  }

  def heartbeat(nodeStatus: NodeStatus): NodeStatus = {
    val heartbeatMillis = System.currentTimeMillis

    val expressionAttrValues = scala.collection.mutable.Map[String, AttributeValue](
      ":next" -> AttributeValue.builder().n(heartbeatMillis.toString).build()
    )

    nodeStatus.heartbeat match {
      case Some(dt) => expressionAttrValues += (":current" -> AttributeValue.builder().n(dt.getMillis.toString).build())
      case None => expressionAttrValues += (":current" -> AttributeValue.builder().nul(true).build())
    }

    try {
      val response = Dynamo.clusterStatusTable.updateItem(
        key = Map("nodeId" -> AttributeValue.builder().n(nodeStatus.nodeId.toString).build()),
        updateExpression = "set #h = :next",
        expressionAttributeNames = Map("#h" -> "heartbeat"),
        expressionAttributeValues = expressionAttrValues.toMap,
        conditionExpression = Some("#h = :current")
      )
      NodeStatus.fromAttributeMap(response.attributes())
    } catch {
      case e: ConditionalCheckFailedException => {
        logger.warn("heartbeat failed", e)
        throw HeartbeatException("heartbeat failed")
      }
    }
  }

  private def registerNewNode(nodeId: Long): NodeStatus = {
    val item = Map(
      "nodeId" -> AttributeValue.builder().n(nodeId.toString).build(),
      "heartbeat" -> AttributeValue.builder().n(System.currentTimeMillis.toString).build()
    )

    try {
      val request = PutItemRequest.builder()
        .tableName(Dynamo.clusterStatusTable.tableName)
        .item(item.asJava)
        .conditionExpression("attribute_not_exists(nodeId)")
        .build()
      Dynamo.client.putItem(request)
      NodeStatus(nodeId, Some(new DateTime(System.currentTimeMillis)))
    } catch {
      case e: ConditionalCheckFailedException => {
        logger.warn("node registration failed", e)
        throw HeartbeatException("registration failed")
      }
    }
  }

  def deregister(nodeStatus: NodeStatus): Unit = {
    logger.info(s"deregistering as node ${nodeStatus.nodeId}")

    try {
      Dynamo.clusterStatusTable.updateItem(
        key = Map("nodeId" -> AttributeValue.builder().n(nodeStatus.nodeId.toString).build()),
        updateExpression = "set #h = :next",
        expressionAttributeNames = Map("#h" -> "heartbeat"),
        expressionAttributeValues = Map(
          ":next" -> AttributeValue.builder().nul(true).build(),
          ":current" -> AttributeValue.builder().n(nodeStatus.heartbeat.get.getMillis.toString).build()
        ),
        conditionExpression = Some("#h = :current")
      )
    } catch {
      case e: ConditionalCheckFailedException => {
        logger.warn("heartbeat failed", e)
        throw HeartbeatException("heartbeat failed")
      }
    }
  }

  def getCurrentState = Dynamo.clusterStatusTable.scan().map(NodeStatus.fromDocument).toList
}

case class NodeStatus(nodeId: Long, heartbeat: Option[DateTime])

object NodeStatus {
  def fromDocument(doc: EnhancedDocument): NodeStatus = {
    val heartbeatLong = if(doc.isNull("heartbeat")) None else Option(doc.getNumber("heartbeat").longValue())
    NodeStatus(
      nodeId = doc.getNumber("nodeId").longValue(),
      heartbeat = heartbeatLong.map(new DateTime(_))
    )
  }

  def fromAttributeMap(attrs: java.util.Map[String, AttributeValue]): NodeStatus = {
    val nodeId = attrs.get("nodeId").n().toLong
    val heartbeat = Option(attrs.get("heartbeat"))
      .filterNot(_.nul() != null && attrs.get("heartbeat").nul())
      .map(_.n().toLong)
      .map(new DateTime(_))
    NodeStatus(nodeId, heartbeat)
  }
}
