package org.downtowndailybread.bethsaida.controller.service.event

import java.time.{ZoneId, ZonedDateTime}
import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.AttendanceAttribute
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}
import org.downtowndailybread.bethsaida.request.{AttendanceRequest, EventRequest}
import spray.json.DefaultJsonProtocol

case class EventAttendance(
  clientId: String,
  eventId: String
)

object EventAttendanceSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val eventAttendanceFormat = jsonFormat2(EventAttendance)
}


trait EventRoutes extends All with Delete with Find with New with Update with ControllerBase {
  this: JsonSupport with AuthenticationProvider with SettingsProvider with DatabaseConnectionProvider =>

  import EventAttendanceSupport._

  val eventRoutes = pathPrefix(JavaUUID / "event") {
    serviceId =>
        event_allForServiceRoute(serviceId) ~
        event_deleteRoute(serviceId) ~
        event_findRoute(serviceId) ~
        event_newRoute(serviceId) ~
        event_updateRoute(serviceId)
  }

  val eventAllRoute = pathPrefix("event") {
    get {
      futureComplete(runSql(c => new EventRequest(settings, c).getAllEvents()))
    }
  }

  val eventAttendRoute = path("event" / "attend") {
    post {
      authorizeNotAnonymous {
        implicit user =>
          entity(as[EventAttendance]) {
            attribs => {
              val now = ZonedDateTime.now(ZoneId.of("America/New_York"))

              futureCompleteCreated(runSql(c => new AttendanceRequest(settings, c)
                  .createAttendance(
                    UUID.fromString(attribs.eventId),
                    UUID.fromString(attribs.clientId),
                    AttendanceAttribute(now, now)
                  )
              ))
            }
          }
      }
    }
  }
}
