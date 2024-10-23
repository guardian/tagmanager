package model

import net.logstash.logback.marker.Markers
import org.joda.time.DateTime
import helpers.JodaDateTimeFormat._
import play.api.Logging

import scala.jdk.CollectionConverters._

trait Audit extends Logging {
  def user: String
  def operation: String
  def description: String
  def date: DateTime

  def auditType: String

  def resourceId: Option[String]
  def message: Option[String]

  def logAudit = logger.logger.info(createMarkers(), "Tag Manager Audit")

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
