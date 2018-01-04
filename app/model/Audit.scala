package model

import play.api.Logger

trait Audit {
 def user: String
 def operation: String
 def description: String
 def auditType: String

 def logAudit = Logger.info(s"User '$user' performed a '$operation' $auditType operation: '$description'")
}
