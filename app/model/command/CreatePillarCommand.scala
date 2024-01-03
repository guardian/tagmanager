package model.command

import com.gu.tagmanagement.{PillarEvent, PillarEventType}
import model._
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}
import repositories._
import CommandError._
import play.api.Logging
import services.KinesisStreams

import scala.concurrent.{Future, ExecutionContext}

case class CreatePillarCommand(
  name: String,
  path: String,
  sectionIds: Seq[String] = Nil) extends Command with Logging {

  type T = Pillar

  def process()(implicit username: Option[String], ec: ExecutionContext): Future[Option[Pillar]] = {

    val pageIdFuture: Future[Long] = Future { try { PathManager.registerPathAndGetPageId(path) } catch {
      case p: PathRegistrationFailed => PathInUse
      case e: Throwable =>
        logger.warn(s"Error using pathmanager for $path: ${e.getMessage}", e)
        throw e
    }}

    val pillarIdFuture = Future{Sequences.pillarId.getNextId}

    for {
      pageId <- pageIdFuture
      pillarId <- pillarIdFuture
    } yield {
      val pillar = Pillar(
        id = pillarId,
        name = name,
        path = path,
        pageId = pageId,
        sectionIds = sectionIds
      )

      val result = PillarRepository.updatePillar(pillar)

      KinesisStreams.pillarUpdateStream.publishUpdate(pillar.id.toString, PillarEvent(PillarEventType.Update, pillar.id, Some(Pillar.asThrift(pillar))))

      PillarAuditRepository.upsertPillarAudit(PillarAudit.created(pillar))

      result
    }
  }
}

object CreatePillarCommand {

  implicit val createPillarCommandFormat: Format[CreatePillarCommand] = (
    (JsPath \ "name").format[String] and
      (JsPath \ "path").format[String] and
      (JsPath \ "sectionIds").format[Seq[String]]

    )(CreatePillarCommand.apply, unlift(CreatePillarCommand.unapply))
}
