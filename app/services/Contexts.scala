package services

import java.util.concurrent.Executors

import play.api.libs.concurrent.Akka

import scala.concurrent.ExecutionContext
import play.api.Play.current

object Contexts {
  implicit val tagOperationContext: ExecutionContext =  play.api.libs.concurrent.Execution.Implicits.defaultContext //Akka.system.dispatchers.lookup("tag-operation-context")
  implicit val capiContext: ExecutionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext // Akka.system.dispatchers.lookup("capi-context")
}
