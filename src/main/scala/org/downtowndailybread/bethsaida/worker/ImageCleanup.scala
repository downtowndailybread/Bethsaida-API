package org.downtowndailybread.bethsaida.worker

import java.time.{LocalDateTime, ZoneOffset}
import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.actor.Actor
import org.downtowndailybread.bethsaida.Settings
import org.downtowndailybread.bethsaida.providers._
import org.downtowndailybread.bethsaida.request.ClientRequest

import collection.JavaConverters._
import scala.concurrent.duration.Duration

class ImageCleanup(val settings: Settings)
  extends Actor with DatabaseConnectionProvider with SettingsProvider with S3Provider {

  implicit val ec = context.dispatcher

  override def preStart(): Unit = {
        context.system.getScheduler.schedule(
          Duration(0, TimeUnit.SECONDS),
          Duration(1, TimeUnit.DAYS),
          self,
          ImageCleanup.CleanUpImages)
  }

  override def receive: Receive = {
    case ImageCleanup.CleanUpImages => cleanup()
  }

  private def cleanup(): Unit = {
    val items = getListOfS3Items().contents().asScala.filter(_.lastModified().isBefore(LocalDateTime.now().minusDays(5).toInstant(ZoneOffset.UTC)))
    val usedUUIDS = runSql(c => new ClientRequest(settings, settings.ds.getConnection).getAllClients()).flatMap{
      r => List(r.photoId, r.clientPhoto).flatten
    }.toSet

    val oldItems = items.groupBy(r => UUID.fromString(r.key().take(36)))
    val toDelete = oldItems.filterNot{
      case (key, _) =>
        usedUUIDS.contains(key)
    }

    val p = toDelete.flatMap(_._2).toList

    deleteFromS3(p)
  }
}

object ImageCleanup {

  object CleanUpImages
}
