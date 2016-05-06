package controllers

import model.Sponsorship
import permissions.TriggerMigrationPermissionsCheck
import play.api.libs.json.Json
import play.api.mvc.Controller
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
}
