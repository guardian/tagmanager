package model.command

import com.gu.tagmanagement._
import model.{TagAudit, Tag}
import play.api.Logger
import repositories.{TagAuditRepository, TagRepository}
import services.KinesisStreams
import org.joda.time.{DateTime, DateTimeZone}
import model.command._


case class UpdateTagCommand(tag: Tag) extends Command {

  type T = Tag

  override def process()(implicit username: Option[String] = None): Option[Tag] = {
    Logger.info(s"updating tag ${tag.id}")
    tag.updatedAt = new DateTime(DateTimeZone.UTC).getMillis

    val existingTag = TagRepository.getTag(tag.id)

    val result = TagRepository.upsertTag(tag)

    KinesisStreams.tagUpdateStream.publishUpdate(tag.id.toString, TagEvent(EventType.Update, tag.id, Some(tag.asThrift)))

    //Need to trigger reindex?

    existingTag foreach {(existing) =>
      if (tag.references != existing.references) {
        Logger.info("Detected references change, triggering reindex")
        FlexTagReindexCommand(tag).process
      }
    }

    TagAuditRepository.upsertTagAudit(TagAudit.updated(tag))

    result
  }
}
