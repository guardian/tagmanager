package modules.sponsorshiplifecycle

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import com.google.common.util.concurrent.AbstractScheduledService.Scheduler
import com.google.common.util.concurrent.{AbstractScheduledService, ServiceManager}
import model.Sponsorship
import play.api.Logging
import play.api.inject.ApplicationLifecycle
import repositories.SponsorshipOperations._
import repositories.SponsorshipRepository

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.jdk.CollectionConverters._

@Singleton
class SponsorshipLifecycleJobs @Inject() (
  lifecycle: ApplicationLifecycle
)(
  implicit val ec: ExecutionContext
) extends Logging {

  import scala.collection.convert.wrapAll._

  logger.info("Starting sponsorship lifecycle jobs")
  lazy val scheduledJobs = List(new SponsorshipLauncher, new SponsorshipExpirer)

  lazy val serviceManager = new ServiceManager(scheduledJobs.asJava)

  serviceManager.startAsync()

  lifecycle.addStopHook{ () => Future(stop) }


  def stop: Unit = {
    logger.info("stopping sponsorship lifecycle jobs")
    logger.info("Requesting stop...")
    serviceManager.stopAsync()
    logger.info("Awaiting stop...")
    serviceManager.awaitStopped(10, TimeUnit.SECONDS)
    logger.info("Stopped")
  }

}

class SponsorshipLauncher(implicit ec: ExecutionContext) extends AbstractScheduledService with Logging {
  override def runOneIteration(): Unit = try {
    implicit val username = Some("Sponsorship launcher")
    logger.debug("checking for sponsorships to launch")

    val sponsorships = SponsorshipRepository.getSponsorshipsToActivate

    sponsorships foreach { s =>
      try {
        logger.info(s"activating sponsorship ${s.sponsorName} ${s.id}")
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
        case NonFatal(e) => logger.error("failed to activate sponsorship", e)
      }
    }
  } catch {
    case NonFatal(e) => logger.error("failed to activate sponsorships", e)
  }

  override def scheduler(): Scheduler = Scheduler.newFixedDelaySchedule(0, 1, TimeUnit.MINUTES)
}

class SponsorshipExpirer(implicit ec: ExecutionContext) extends AbstractScheduledService with Logging {
  implicit val username = Some("Sponsorship expirer")

  override def runOneIteration(): Unit = try {

    logger.debug("checking for sponsorships to expire")
    val sponsorships = SponsorshipRepository.getSponsorshipsToExpire

    sponsorships foreach { s =>
      try {
        logger.info(s"expiring sponsorship ${s.sponsorName} ${s.id}")
        if(s.sponsorshipType == "paidContent") {
          expirePaidContent(s)
        } else {
          expireSponsorship(s)
        }
      } catch {
        case NonFatal(e) => logger.error("failed to expire sponsorship", e)
      }
    }
  } catch {
    case NonFatal(e) => logger.error("failed to expire sponsorships", e)
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
