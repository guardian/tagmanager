package model.command

import com.gu.tagmanagement._
import model.{DenormalisedTag, SectionAudit, Tag, TagAudit}
import helpers.JodaDateTimeFormat._
import org.joda.time.{DateTime, DateTimeZone}
import play.api.Logging
import repositories._
import services.KinesisStreams

import scala.concurrent.{Future, ExecutionContext}


case class UpdateTagCommand(denormalisedTag: DenormalisedTag) extends Command with Logging {

  type T = Tag

  override def process()(implicit username: Option[String], ec: ExecutionContext): Future[Option[Tag]] = Future{
    val (tag, sponsorship) = denormalisedTag.normalise()

    logger.info(s"updating tag ${tag.id}")
    tag.updatedAt = new DateTime(DateTimeZone.UTC).getMillis

    val existingTag = TagRepository.getTag(tag.id)

    val result = TagRepository.upsertTag(tag)

    sponsorship.foreach { spons =>
      SponsorshipRepository.updateSponsorship(spons)

      // trigger section reindex to get sponsorship changes
      for (
        sections <- spons.sections;
        sectionId <- sections;
        section <- SectionRepository.getSection(sectionId)
      ) {
        KinesisStreams.sectionUpdateStream.publishUpdate(section.id.toString, SectionEvent(EventType.Update, section.id, Some(section.asThrift)))
      }

    }

    if (tag.`type` == "PaidContent" && tag.externalName != existingTag.map(_.externalName).getOrElse("")) {
      logger.debug("paid content tag name changed, checking checking section")
      for (
        sectionId <- tag.section;
        section <- SectionRepository.getSection(sectionId)
        if(section.sectionTagId == tag.id)
      ) {
        logger.info(s"microsite's primary paidContent tag external name updated, updating section name (tag ${tag.id}, section ${sectionId} name ${tag.externalName})")
        val renamedSection = section.copy(name = tag.externalName)
        val updatedSection = SectionRepository.updateSection(renamedSection)

        updatedSection foreach{ s =>
          KinesisStreams.sectionUpdateStream.publishUpdate(s.id.toString, SectionEvent(EventType.Update, s.id, Some(s.asThrift)))
          SectionAuditRepository.upsertSectionAudit(SectionAudit.updated(s))
        }
      }
    }

    KinesisStreams.tagUpdateStream.publishUpdate(tag.id.toString, TagEvent(EventType.Update, tag.id, Some(tag.asThrift)))

    existingTag foreach {(existing) =>
      if (tag.externalReferences != existing.externalReferences) {
        logger.info("Detected references change, triggering reindex")
        FlexTagReindexCommand(tag).process()
      }
    }

    TagAuditRepository.upsertTagAudit(TagAudit.updated(tag))

    result
  }
}
