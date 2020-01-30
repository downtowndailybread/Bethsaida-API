package org.downtowndailybread.bethsaida.worker

import java.time.ZonedDateTime
import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.actor.Actor
import org.downtowndailybread.bethsaida.Settings
import org.downtowndailybread.bethsaida.model.{AnonymousUser, EventAttribute, HoursOfOperation, Service, ServiceAttributes}
import org.downtowndailybread.bethsaida.providers.{DatabaseConnectionProvider, SettingsProvider}
import org.downtowndailybread.bethsaida.request.{EventRequest, ServiceRequest}
import org.downtowndailybread.bethsaida.worker.EventScheduler.{CheckUpcomingEvents, ScheduleEvent}

import scala.collection.mutable
import scala.concurrent.duration.Duration

class EventScheduler(val settings: Settings) extends Actor with DatabaseConnectionProvider with SettingsProvider {

  implicit val ec = context.dispatcher

  override def preStart(): Unit = {
//    context.system.getScheduler.schedule(
//      Duration(0, TimeUnit.SECONDS),
//      Duration(10, TimeUnit.MINUTES),
//      self,
//      CheckUpcomingEvents)
  }

  override def receive: Receive = {

    case CheckUpcomingEvents => checkUpcomingEvents(ZonedDateTime.now(settings.timezone.toZoneId))
    case s@ScheduleEvent(serviceId, attributes, opening, scheduleId) =>
      runSql(conn => new EventRequest(settings, conn).createEvent(
        serviceId,
        EventAttribute(opening, attributes.defaultCapacity, None, Some(scheduleId))
      )(AnonymousUser))
      scheduledEvents.remove(s)

  }

  def checkUpcomingEvents(implicit localDateTime: ZonedDateTime): Unit = {
    val services = runSql(c => new ServiceRequest(settings, settings.ds.getConnection).getAllServices())
    val serviceOpenings = services.map(r =>
      (r,
        r.schedules.flatMap(r =>
          r.detail
            .getSchedules
            .takeWhile(r => r.start.isAfter(localDateTime.minusHours(2)))
            .map(s => (r, s))
            .sortBy(_._2.start.toEpochSecond)
        )
      )
    ).filter(_._2.nonEmpty)

    serviceOpenings.foreach {
      case (service, openingList) => openingList.foreach {
        case (schedule, opening) =>
          val scheduleEvent = ScheduleEvent(service.id, service.attributes, opening, schedule.id)
          if(!scheduledEvents.contains(scheduleEvent)) {
            context.system.getScheduler.scheduleOnce(Duration(
              localDateTime.toEpochSecond - opening.start.minusHours(1).toEpochSecond, TimeUnit.SECONDS
            ), self, scheduleEvent)
            scheduledEvents.add(scheduleEvent)
          }
      }
    }
  }

  private val scheduledEvents = mutable.Set[ScheduleEvent]()

}


object EventScheduler {

  case object CheckUpcomingEvents

  case class ScheduleEvent(
                            serviceId: UUID,
                            serviceAttributes: ServiceAttributes,
                            opening: HoursOfOperation,
                            scheduleId: UUID
                          )

}