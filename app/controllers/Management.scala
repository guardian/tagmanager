package controllers

import play.api.mvc.{Action, Controller}

object Management extends Controller {

  def healthCheck = Action {
    Ok("OK")
  }

}