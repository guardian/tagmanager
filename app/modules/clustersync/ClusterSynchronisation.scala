package modules.clustersync

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import javax.inject._
import play.api.Logging
import repositories.{TagLookupCache, SectionLookupCache}
import services.{Config, KinesisConsumer}

import scala.collection.convert.wrapAll._

import com.google.common.util.concurrent.{ServiceManager, AbstractScheduledService}
import com.google.common.util.concurrent.AbstractScheduledService.Scheduler
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.jdk.CollectionConverters._

@Singleton
class ClusterSynchronisation @Inject() (lifecycle: ApplicationLifecycle) extends Logging {

  val serviceManager = new ServiceManager(List(new NodeStatusHeartbeater(this)).asJava)

  val reservation: AtomicReference[Option[NodeStatus]] = new AtomicReference[Option[NodeStatus]](None)
  val tagCacheSynchroniser: AtomicReference[Option[KinesisConsumer]] = new AtomicReference[Option[KinesisConsumer]](None)
  val sectionCacheSynchroniser: AtomicReference[Option[KinesisConsumer]] = new
    AtomicReference[Option[KinesisConsumer]](None)


  lifecycle.addStopHook{ () => Future.successful(stop) }
  serviceManager.startAsync()

  initialise

  def initialise: Unit = {
    try {
      logger.info("starting sync components...")
      val ns = NodeStatusRepository.register()
      reservation.set(Some(ns))

      logger.info("loading tag cache")
      TagLookupCache.refresh

      val appName = s"tag-cache-syncroniser-${Config().aws.stage}-${ns.nodeId}"
      logger.info(s"Starting tag sync kinesis consumer with appName: $appName")

      val tagUpdateConsumer = new KinesisConsumer(Config().tagUpdateStreamName, appName, TagSyncUpdateProcessor)
      logger.info("starting tag sync consumer")
      tagUpdateConsumer.start()
      tagCacheSynchroniser.set(Some(tagUpdateConsumer))
    } catch {
      case he: HeartbeatException => logger.error("failed to register in the cluster, will try again next heartbeat")
      case NonFatal(e) => {
        logger.error("failed to start sync", e)
        pause
      }
    }
  }

  def pause: Unit = {
    logger.warn("pausing cluster synchronisation")
    tagCacheSynchroniser.get.foreach{consumer =>
      logger.warn("stopping consumer")
      consumer.stop()
      tagCacheSynchroniser.set(None)
    }

    reservation.get.foreach{ns =>
      logger.warn("deregistering node")
      NodeStatusRepository.deregister(ns)
      reservation.set(None)
    }
  }

  def heartbeat: Unit = {
    try {
      reservation.get() match {
        case Some(ns) => {
          try {
            logger.info(s"heartbeating as node ${ns.nodeId}...")
            reservation.set(Some(NodeStatusRepository.heartbeat(ns)))
          } catch {
            case he: HeartbeatException => {
              logger.error("heartbeat failed", he)
              pause
            }
          }
        }
        case None => initialise
      }
    } catch {
      case NonFatal(e) => logger.error("Error heartbeating", e)
    }
  }

  def stop(): Unit = {
    logger.info("shutting down synchronisation")
    logger.info("stopping heartbeater")
    serviceManager.stopAsync()

    logger.info("awaiting service runner stop")
    serviceManager.awaitStopped(10, TimeUnit.SECONDS)

    logger.info("heartbeater stopped, stopping tag cache sync")
    tagCacheSynchroniser.get foreach { consumer => consumer.stop() }

    logger.info("deregistering node")
    reservation.get foreach { ns => NodeStatusRepository.deregister(ns) }

    logger.info("synchronisation shutdown complete")
  }
}

class NodeStatusHeartbeater(clusterSyncronisation: ClusterSynchronisation) extends AbstractScheduledService {

  override def runOneIteration(): Unit = clusterSyncronisation.heartbeat

  override def scheduler(): Scheduler = Scheduler.newFixedDelaySchedule(1, 1, TimeUnit.MINUTES)
}
