package model.command

import com.gu.tagmanagement.{PillarEvent, PillarEventType}
import repositories._
import services.KinesisStreams

import scala.concurrent.{Future, ExecutionContext}


case class DeletePillarCommand(id: Long) extends Command {
  override type T = Long

  override def process()(implicit username: Option[String], ec: ExecutionContext): Future[Option[Long]] = Future {
    for {
      pillar <- PillarRepository.getPillar(id)
      _ <- PillarRepository.deletePillar(id)
    } yield {
      KinesisStreams.pillarUpdateStream.publishUpdate(id.toString, PillarEvent(PillarEventType.Delete, id, None))
      PathManager.removePathForId(pillar.pageId)
      id
    }

  }
}
