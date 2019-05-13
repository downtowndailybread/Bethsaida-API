package org.downtowndailybread.bethsaida.controller.service.event

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.request.EventRequest
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}

trait Delete extends ControllerBase {
  this: JsonSupport with DatabaseConnectionProvider with SettingsProvider with AuthenticationProvider =>

  val event_deleteRoute = path(JavaUUID / "event" / JavaUUID / "delete") {
    (serviceId, eventId) =>
      authorize(_ => true) {
        implicit iu =>
          post {
            futureComplete {
              runSql(c =>
                new EventRequest(settings, c).deleteEvent(serviceId, eventId))
              "event deleted"
            }
          }
      }
  }
}
