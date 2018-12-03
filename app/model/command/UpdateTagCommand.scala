package model.command

import com.gu.tagmanagement._
import model.{DenormalisedTag, SectionAudit, Tag, TagAudit}
import org.joda.time.{DateTime, DateTimeZone}
import play.api.Logger
import repositories._
import services.{Contexts, KinesisStreams}

import scala.concurrent.Future


case class UpdateTagCommand(denormalisedTag: DenormalisedTag) extends Command {

  type T = Tag

  override def process()(implicit username: Option[String] = None): Future[Option[Tag]] = Future{
    val (tag, sponsorship) = denormalisedTag.normalise()

    Logger.info(s"updating tag ${tag.id}")
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
      Logger.debug("paid content tag name changed, checking checking section")
      for (
        sectionId <- tag.section;
        section <- SectionRepository.getSection(sectionId)
        if(section.sectionTagId == tag.id)
      ) {
        Logger.info(s"microsite's primary paidContent tag external name updated, updating section name (tag ${tag.id}, section ${sectionId} name ${tag.externalName})")
        val renamedSection = section.copy(name = tag.externalName)
        val updatedSection = SectionRepository.updateSection(renamedSection)

        updatedSection foreach{ s =>
          KinesisStreams.sectionUpdateStream.publishUpdate(s.id.toString, SectionEvent(EventType.Update, s.id, Some(s.asThrift)))
          SectionAuditRepository.upsertSectionAudit(SectionAudit.updated(s))
        }
      }
    }
    
    KinesisStreams.tagUpdateStream.publishUpdate(tag.id.toString, TagEvent(EventType.Update, tag.id, Some(tag.asThrift)))

    existingTag foreach { existing =>

      val referenceHasChanged = tag.externalReferences.exists { reference =>
        val isACapiExternalReference = ExternalReferencesTypeRepository.getReferenceType(reference.`type`).exists(_.capiType.isDefined)
        val newOrUpdated = !existing.externalReferences.contains(reference)

        isACapiExternalReference && newOrUpdated
      }

      if (referenceHasChanged) {
        Logger.info("Detected references change, triggering reindex")
        FlexTagReindexCommand(tag).process
      }
    }

    TagAuditRepository.upsertTagAudit(TagAudit.updated(tag))

    result
  }(Contexts.tagOperationContext)
}
