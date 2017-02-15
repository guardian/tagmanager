package services

import play.api.libs.concurrent.Akka

import scala.concurrent.ExecutionContext
import play.api.Play.current

object Contexts {
  implicit val tagOperationContext: ExecutionContext = Akka.system.dispatchers.lookup("tag-operation-context")
  implicit val capiContext: ExecutionContext = Akka.system.dispatchers.lookup("capi-context")
}
