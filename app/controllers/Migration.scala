package controllers

import model.{Sponsorship, Tag}
import permissions.TriggerMigrationPermissionsCheck
import play.api.libs.json.Json
import play.api.mvc.Controller
import repositories._
import services.migration.PaidContentMigrator

import scala.io.Source

object Migration extends Controller with PanDomainAuthActions {

  def showPaidContentUploadForm = (APIAuthAction andThen TriggerMigrationPermissionsCheck) {
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

  def movePaidcontentSponsorshipUpToSection = (APIAuthAction andThen TriggerMigrationPermissionsCheck) {

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

  def flattenSponsoredMicrosite(sponsorshipId: Long) =  (APIAuthAction andThen TriggerMigrationPermissionsCheck) {
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

}
