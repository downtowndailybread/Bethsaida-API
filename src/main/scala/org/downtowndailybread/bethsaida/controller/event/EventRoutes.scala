package org.downtowndailybread.bethsaida.controller.event

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}
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


  val allEventRoutes = {
    pathPrefix("event") {

      val eventRoutes = event_allRoute ~ event_newRoute ~ event_allActiveRoute ~ event_findRoute ~ event_updateRoute


      eventRoutes

    }
  }
}
