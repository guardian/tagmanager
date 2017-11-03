package model.command

import com.gu.tagmanagement.{PillarEvent, PillarEventType}
import repositories._
import services.{Contexts, KinesisStreams}

import scala.concurrent.Future


case class DeletePillarCommand(id: Long) extends Command {
  override type T = Long

  override def process()(implicit username: Option[String] = None): Future[Option[Long]] = Future {
    KinesisStreams.pillarUpdateStream.publishUpdate(id.toString, PillarEvent(PillarEventType.Delete, id, None))

    PillarRepository.deletePillar(id)

  }(Contexts.tagOperationContext)
}
