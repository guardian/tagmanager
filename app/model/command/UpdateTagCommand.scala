package model.command

import com.gu.tagmanagement._
import model.{DenormalisedTag, Tag, TagAudit}
import play.api.Logger
import repositories.{SectionRepository, SponsorshipRepository, TagAuditRepository, TagRepository}
import services.KinesisStreams
import org.joda.time.{DateTime, DateTimeZone}
import model.command._


case class UpdateTagCommand(denormalisedTag: DenormalisedTag) extends Command {

  type T = Tag

  override def process()(implicit username: Option[String] = None): Option[Tag] = {
    val (tag, sponsorship) = denormalisedTag.normalise()

    Logger.info(s"updating tag ${tag.id}")
    tag.updatedAt = new DateTime(DateTimeZone.UTC).getMillis

    val existingTag = TagRepository.getTag(tag.id)

    val result = TagRepository.upsertTag(tag)

    sponsorship.foreach{ spons =>
      SponsorshipRepository.updateSponsorship(spons)

      // trigger section reindex to get sponsorship changes
      for(
        sections <- spons.sections;
        sectionId <- sections;
        section <- SectionRepository.getSection(sectionId)
      ) {
        KinesisStreams.sectionUpdateStream.publishUpdate(section.id.toString, SectionEvent(EventType.Update, section.id, Some(section.asThrift)))
      }

    }
    
    KinesisStreams.tagUpdateStream.publishUpdate(tag.id.toString, TagEvent(EventType.Update, tag.id, Some(tag.asThrift)))

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
