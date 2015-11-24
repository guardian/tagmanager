package model.jobs

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Format}
import repositories.ContentAPI


case class BatchTagAddCompleteCheck(contentIds: List[String], apiTagId: String, completed: Int = 0) extends Step {
  val count = contentIds.size

  def process = {
    val currentlyCompleted = ContentAPI.countOccurencesOfTagInContents(contentIds, apiTagId)

    if (currentlyCompleted == count) {
      None
    } else {
      Some(copy(completed = currentlyCompleted))
    }
  }
}

case class BatchTagRemoveCompleteCheck(contentIds: List[String], apiTagId: String, completed: Int = 0) extends Step {
  val count = contentIds.size

  def process = {
    val currentlyCompleted = count - ContentAPI.countOccurencesOfTagInContents(contentIds, apiTagId)

    if (currentlyCompleted == count) {
      None
    } else {
      Some(copy(completed = currentlyCompleted))
    }
  }
}



object BatchTagAddCompleteCheck {
  implicit val batchTagAddCompleteCheck: Format[BatchTagAddCompleteCheck] = (
    (JsPath \ "contentIds").formatNullable[List[String]].inmap[List[String]](_.getOrElse(Nil), Some(_)) and
      (JsPath \ "apiTagId").format[String] and
      (JsPath \ "completes").format[Int]
    )(BatchTagAddCompleteCheck.apply, unlift(BatchTagAddCompleteCheck.unapply))
}

object BatchTagRemoveCompleteCheck {
  implicit val batchTagRemoveCompleteCheck: Format[BatchTagRemoveCompleteCheck] = (
    (JsPath \ "contentIds").formatNullable[List[String]].inmap[List[String]](_.getOrElse(Nil), Some(_)) and
      (JsPath \ "apiTagId").format[String] and
      (JsPath \ "completes").format[Int]
    )(BatchTagRemoveCompleteCheck.apply, unlift(BatchTagRemoveCompleteCheck.unapply))
}
