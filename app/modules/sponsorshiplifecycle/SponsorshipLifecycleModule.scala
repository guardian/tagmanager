package modules.sponsorshiplifecycle

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}

import com.google.common.util.concurrent.AbstractScheduledService.Scheduler
import com.google.common.util.concurrent.{AbstractScheduledService, ServiceManager}
import com.google.inject.AbstractModule
import model.Sponsorship
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import play.api.libs.concurrent.Execution.Implicits._
import repositories.SponsorshipOperations._
import repositories.SponsorshipRepository

import scala.concurrent.Future
import scala.util.control.NonFatal

class SponsorshipLifecycleModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[SponsorshipLifecycleJobs]).asEagerSingleton()
  }
}

@Singleton
class SponsorshipLifecycleJobs @Inject() (lifecycle: ApplicationLifecycle) {

  import scala.collection.convert.wrapAll._

  Logger.info("Starting sponsorship lifecycle jobs")
  lazy val scheduledJobs = List(new SponsorshipLauncher, new SponsorshipExpirer)

  lazy val serviceManager = new ServiceManager(scheduledJobs)

  serviceManager.startAsync()

  lifecycle.addStopHook{ () => Future(stop) }


  def stop: Unit = {
    Logger.info("stopping sponsorship lifecycle jobs")
    Logger.info("Requesting stop...")
    serviceManager.stopAsync()
    Logger.info("Awaiting stop...")
    serviceManager.awaitStopped(10, TimeUnit.SECONDS)
    Logger.info("Stopped")
  }

}

class SponsorshipLauncher extends AbstractScheduledService {
  override def runOneIteration(): Unit = try {
    implicit val username = Some("Sponsorship launcher")
    Logger.debug("checking for sponsorships to launch")

    val sponsorships = SponsorshipRepository.getSponsorshipsToActivate

    sponsorships foreach { s =>
      try {
        Logger.info(s"activating sponsorship ${s.sponsorName} ${s.id}")
        val activated = s.copy(status = "active")
        SponsorshipRepository.updateSponsorship(activated)

        for(
          tags <- s.tags;
          tagId <- tags
        ) {
          addSponsorshipToTag(s.id, tagId)
          // If the sponsorship type is paid content then we also need to go and unexpire any associated tags
          // If it is a sponsorship or foundation type sponsor then the tags won't have been expired in the first place.
          if (activated.sponsorshipType == "paidContent") {
            unexpirePaidContentTag(tagId)
          }
        }
        for(
          sections <- s.sections;
          sectionId <- sections
        ) {
          addSponsorshipToSection(s.id, sectionId)
        }
      } catch {
        case NonFatal(e) => Logger.error("failed to activate sponsorship", e)
      }
    }
  } catch {
    case NonFatal(e) => Logger.error("failed to activate sponsorships", e)
  }

  override def scheduler(): Scheduler = Scheduler.newFixedDelaySchedule(0, 1, TimeUnit.MINUTES)
}

class SponsorshipExpirer extends AbstractScheduledService {
  implicit val username = Some("Sponsorship expirer")

  override def runOneIteration(): Unit = try {

    Logger.debug("checking for sponsorships to expire")
    val sponsorships = SponsorshipRepository.getSponsorshipsToExpire

    sponsorships foreach { s =>
      try {
        Logger.info(s"expiring sponsorship ${s.sponsorName} ${s.id}")
        if(s.sponsorshipType == "paidContent") {
          expirePaidContent(s)
        } else {
          expireSponsorship(s)
        }
      } catch {
        case NonFatal(e) => Logger.error("failed to expire sponsorship", e)
      }
    }
  } catch {
    case NonFatal(e) => Logger.error("failed to expire sponsorships", e)
  }


  private def expirePaidContent(s: Sponsorship): Unit = {
    val expired = s.copy(status = "expired")
    SponsorshipRepository.updateSponsorship(expired)
    for(
      tags <- s.tags;
      tagId <- tags
    ) {
      expirePaidContentTag(tagId)
    }
    for(
      sections <- s.sections;
      sectionId <- sections
    ) {
      removeSponsorshipFromSection(s.id, sectionId)
    }
  }

  private def expireSponsorship(s: Sponsorship): Unit = {
    val expired = s.copy(status = "expired")
    SponsorshipRepository.updateSponsorship(expired)
    for(
      tags <- s.tags;
      tagId <- tags
    ) {
      removeSponsorshipFromTag(s.id, tagId)
    }
    for(
      sections <- s.sections;
      sectionId <- sections
    ) {
      removeSponsorshipFromSection(s.id, sectionId)
    }
  }

  override def scheduler(): Scheduler = Scheduler.newFixedDelaySchedule(0, 1, TimeUnit.MINUTES)
}