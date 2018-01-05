package model

import net.logstash.logback.marker.Markers
import org.joda.time.DateTime
import play.api.Logger

import scala.collection.JavaConverters._

trait Audit {
  def user: String
  def operation: String
  def description: String
  def date: DateTime

  def auditType: String

  def resourceId: Option[String]
  def message: Option[String]

  def logAudit = Logger.logger.info(createMarkers(), "Tag Manager Audit")

  private def createMarkers() =
    Markers.appendEntries((
        Map(
          "type" -> auditType,
          "operation" -> operation,
          "userEmail" -> user,
          "date" -> date.toString,
          "shortMessage" -> description
        )
          ++ message.map("message" -> _)
          ++ resourceId.map("resourceId" -> _)
      ).asJava
    )
}
