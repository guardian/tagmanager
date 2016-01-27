package modules.clustersync

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import javax.inject._
import play.api.Logger
import repositories.TagLookupCache
import services.{Config, KinesisConsumer}

import scala.collection.convert.wrapAll._

import com.google.common.util.concurrent.{ServiceManager, AbstractScheduledService}
import com.google.common.util.concurrent.AbstractScheduledService.Scheduler
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future
import scala.util.control.NonFatal

@Singleton
class ClusterSynchronisation @Inject() (lifecycle: ApplicationLifecycle) {

  val serviceManager = new ServiceManager(List(new NodeStatusHeartbeater(this)))

  val reservation: AtomicReference[Option[NodeStatus]] = new AtomicReference[Option[NodeStatus]](None)
  val tagCacheSynchroniser: AtomicReference[Option[KinesisConsumer]] = new AtomicReference[Option[KinesisConsumer]](None)

  lifecycle.addStopHook{ () => Future.successful(stop) }
  serviceManager.startAsync()

  initialise

  def initialise {
    try {
      Logger.info("starting sync components...")
      val ns = NodeStatusRepository.register()
      reservation.set(Some(ns))

      Logger.info("loading tag cache")
      TagLookupCache.refresh

      val tagUpdateConsumer = new KinesisConsumer(Config().tagUpdateStreamName, s"tag-cache-syncroniser-${ns.nodeId}", TagSyncUpdateProcessor)
      Logger.info("starting sync consumer")
      tagUpdateConsumer.start()
      tagCacheSynchroniser.set(Some(tagUpdateConsumer))
    } catch {
      case he: HeartbeatException => Logger.error("failed to register in the cluster, will try again next heartbeat")
      case NonFatal(e) => {
        Logger.error("failed to start sync", e)
        pause
      }
    }
  }

  def pause {
    Logger.warn("pausing cluster synchronisation")
    tagCacheSynchroniser.get.foreach{consumer =>
      Logger.warn("stopping consumer")
      consumer.stop()
      tagCacheSynchroniser.set(None)
    }

    reservation.get.foreach{ns =>
      Logger.warn("deregistering node")
      NodeStatusRepository.deregister(ns)
      reservation.set(None)
    }
  }

  def heartbeat {
    try {
      reservation.get() match {
        case Some(ns) => {
          try {
            Logger.info(s"heartbeating as node ${ns.nodeId}...")
            reservation.set(Some(NodeStatusRepository.heartbeat(ns)))
          } catch {
            case he: HeartbeatException => {
              Logger.error("heartbeat failed", he)
              pause
            }
          }
        }
        case None => initialise
      }
    } catch {
      case NonFatal(e) => Logger.error("Error heartbeating", e)
    }
  }

  def stop(): Unit = {
    Logger.info("shutting down synchronisation")
    Logger.info("stopping heartbeater")
    serviceManager.stopAsync()

    Logger.info("awaiting service runner stop")
    serviceManager.awaitStopped(10, TimeUnit.SECONDS)

    Logger.info("heartbeater stopped, stopping tag cache sync")
    tagCacheSynchroniser.get foreach { consumer => consumer.stop() }

    Logger.info("deregistering node")
    reservation.get foreach { ns => NodeStatusRepository.deregister(ns) }

    Logger.info("synchronisation shutdown complete")
  }
}

class NodeStatusHeartbeater(clusterSyncronisation: ClusterSynchronisation) extends AbstractScheduledService {

  override def runOneIteration(): Unit = clusterSyncronisation.heartbeat

  override def scheduler(): Scheduler = Scheduler.newFixedDelaySchedule(1, 1, TimeUnit.MINUTES)
}