package controllers

import com.gu.pandomainauth.PanDomainAuthSettingsRefresher
import com.gu.tagmanagement.{EventType, TagEvent}
import model.command.FlexTagReindexCommand
import model.{PaidContentInformation, Sponsorship, TagAudit}
import permissions.TriggerMigrationPermissionsCheck
import play.api.Logging
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.{BaseController, ControllerComponents}
import repositories._
import services.KinesisStreams
import services.migration.PaidContentMigrator

import scala.concurrent.ExecutionContext
import scala.io.Source

class Migration(
  val wsClient: WSClient,
  override val controllerComponents: ControllerComponents,
  val panDomainSettings: PanDomainAuthSettingsRefresher
)(
  implicit ec: ExecutionContext
)
  extends BaseController
  with PanDomainAuthActions
  with Logging {

  def showPaidContentUploadForm = (APIAuthAction andThen TriggerMigrationPermissionsCheck()) {
    Ok(views.html.Application.migration.paidContentUploadForm())
  }

  def migratePaidContent = APIAuthAction(parse.multipartFormData) { req =>
    req.body.file("migrationFile").map{ jsonFile =>
      val jsonString = Source.fromFile(jsonFile.ref.file, "UTF-8").getLines().mkString("\n")

      val json = Json.parse(jsonString)
      val sponsorships = json.as[List[Sponsorship]]

      sponsorships.foreach { sponsorship =>
        PaidContentMigrator.migrate(sponsorship)
      }

      Ok(s"Migrated ${sponsorships.length} tags to paid content type")
    }.getOrElse(BadRequest("unable to read file"))
  }

  def movePaidcontentSponsorshipUpToSection = (APIAuthAction andThen TriggerMigrationPermissionsCheck()) {

    implicit val username = Some("sponsorship Migration")

    val paidContentTags = TagLookupCache.search(new TagSearchCriteria(types = Some(List("PaidContent"))))

    var count = 0

    paidContentTags foreach{ tag =>
      val section = tag.section.flatMap{sid => SectionRepository.getSection(sid)}
      section foreach { s =>
        if (s.sectionTagId == tag.id) {
          val sponsorship = tag.sponsorship.flatMap{sid => SponsorshipRepository.getSponsorship(sid)}

          val targetedSponsorship = sponsorship map { spon =>
            val targetSections = spon.sections.getOrElse(Nil)
            val updatedSponsorship = spon.copy(sections = Some((s.id :: targetSections).distinct))

            SponsorshipRepository.updateSponsorship(updatedSponsorship)

            if(updatedSponsorship.status == "active"){
              SponsorshipOperations.addSponsorshipToSection(updatedSponsorship.id, s.id)
              count = count + 1
            }
          }

        }
      }
    }

    Ok(s"updated $count sections")
  }

  def flattenSponsoredMicrosite(sponsorshipId: Long) =  (APIAuthAction andThen TriggerMigrationPermissionsCheck()) {
    implicit val username = Some("sponsorship Migration")

    val spons = SponsorshipRepository.getSponsorship(sponsorshipId)

    var count = 0
    spons.foreach { s =>
      PaidContentMigrator.migrate(s)
      for (
        tags <- s.tags;
        tagId <- tags;
        tag <- TagRepository.getTag(tagId)
      ) {
        val section = tag.section.flatMap { sid => SectionRepository.getSection(sid) }
        section foreach { s =>
          if (s.sectionTagId == tag.id) {

            val sponsorship = tag.sponsorship.flatMap { sid => SponsorshipRepository.getSponsorship(sid) }

            val targetedSponsorship = sponsorship map { spon =>

              val targetSections = spon.sections.getOrElse(Nil)
              val updatedSponsorship = spon.copy(sections = Some((s.id :: targetSections).distinct))

              SponsorshipRepository.updateSponsorship(updatedSponsorship)

              if (updatedSponsorship.status == "active") {
                SponsorshipOperations.addSponsorshipToSection(updatedSponsorship.id, s.id)
                count = count + 1
              }
            }

          }
        }
      }
    }

    Ok(s"updated $count partner zones")
  }

  def addMissingPaidContentTypes() = (APIAuthAction andThen TriggerMigrationPermissionsCheck()) {
    implicit val username = Some("sponsorship Migration")

    val paidContentTags = TagLookupCache.search(
      TagSearchCriteria(types = Some(List("PaidContent")))
    )

    val missingSubType = paidContentTags.filter{t =>
      t.paidContentInformation.isEmpty ||
        t.paidContentInformation.get.paidContentType == null ||
        t.paidContentInformation.get.paidContentType.trim == ""
    }

    missingSubType foreach{ t =>
      val paidContentInformation = t.paidContentInformation match {
        case Some(pc) => pc.copy(paidContentType = "Topic")
        case None => PaidContentInformation(paidContentType = "Topic", campaignColour = None)
      }
      val withSubType = t.copy(paidContentInformation = Some(paidContentInformation))

      TagRepository.upsertTag(withSubType) foreach { updatedTag =>
        KinesisStreams.tagUpdateStream.publishUpdate(updatedTag.id.toString, TagEvent(EventType.Update, updatedTag.id, Some(updatedTag.asThrift)))
        TagAuditRepository.upsertTagAudit(TagAudit.updated(updatedTag))
        FlexTagReindexCommand(updatedTag).process()
      }

    }

    Ok(missingSubType.map{t => t.externalName}.mkString("\n"))
  }

  def dudupeActiveSponsorships() = (APIAuthAction andThen TriggerMigrationPermissionsCheck()) {
    implicit val username = Some("sponsorship Migration")

    val allTags = TagLookupCache.allTags.get

    val tagsWithDuplicatSpons = allTags.filter{t =>
      t.activeSponsorships.length != t.activeSponsorships.distinct.length
    }

    for (
      t <- tagsWithDuplicatSpons;
      spons <- t.activeSponsorships.headOption
    ){
      // this is a massive cheat, the add operation not enforces distinctness so adding an already present sponsorship
      // will cause the list to dedupe and the correct indexing messages to be sent.
      SponsorshipOperations.addSponsorshipToTag(spons, t.id)
    }

    Ok(s"removed ${tagsWithDuplicatSpons.length} dupes")
  }

}
