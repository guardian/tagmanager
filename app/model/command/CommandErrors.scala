package model.command

import play.api.mvc.{Results, Result}


case class CommandError(message: String, responseCode: Int) extends RuntimeException(message)

object CommandError extends Results {

  def SectionNotFound = throw new CommandError("section not found", 400)
  def EditionNotFound = throw new CommandError("edition not found", 400)
  def TagNotFound = throw new CommandError("tag not found", 400)
  def PathInUse = throw new CommandError("path in use", 400)
  def PathNotFound = throw new CommandError("could not remove path from pathmanager", 400)

  def InvalidSectionEditionRegion = throw new CommandError("Invalid region supplied", 400)

  def commandErrorAsResult: PartialFunction[Throwable, Result] = {
    case CommandError(msg, 400) => BadRequest(msg)
    case CommandError(msg, 404) => NotFound
  }
}
