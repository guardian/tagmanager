package model.command

import com.gu.tagmanagement.{EventType, TagEvent}
import model.{TagAudit, Tag}
import play.api.Logger
import repositories.{TagAuditRepository, TagRepository}
import services.KinesisStreams


case class UpdateTagCommand(tag: Tag) extends Command {

  type T = Tag

  override def process()(implicit username: Option[String] = None): Option[Tag] = {
    Logger.info(s"updating tag ${tag.id}")

    val result = TagRepository.upsertTag(tag)

    KinesisStreams.tagUpdateStream.publishUpdate(tag.id.toString, TagEvent(EventType.Update, tag.id, Some(tag.asThrift)))

    TagAuditRepository.upsertTagAudit(TagAudit.updated(tag))

    result
  }
}
