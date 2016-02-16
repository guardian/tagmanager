package model.command

import com.gu.tagmanagement._
import model.{DenormalisedTag, TagAudit, Tag}
import play.api.Logger
import repositories.{SponsorshipRepository, TagAuditRepository, TagRepository}
import services.KinesisStreams
import model.command._


case class UpdateTagCommand(denormalisedTag: DenormalisedTag) extends Command {

  type T = Tag

  override def process()(implicit username: Option[String] = None): Option[Tag] = {
    val (tag, sponsorship) = denormalisedTag.normalise()

    Logger.info(s"updating tag ${tag.id}")

    val existingTag = TagRepository.getTag(tag.id)

    val result = TagRepository.upsertTag(tag)

    KinesisStreams.tagUpdateStream.publishUpdate(tag.id.toString, TagEvent(EventType.Update, tag.id, Some(tag.asThrift)))

    sponsorship.foreach( SponsorshipRepository.updateSponsorship )
    //Need to trigger reindex?

    existingTag foreach {(existing) =>
      if (tag.externalReferences != existing.externalReferences) {
        Logger.info("Detected references change, triggering reindex")
        FlexTagReindexCommand(tag).process
      }
    }

    TagAuditRepository.upsertTagAudit(TagAudit.updated(tag))

    result
  }
}
