package org.downtowndailybread.bethsaida.controller.service.event

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.request.EventRequest
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}

trait Find extends ControllerBase {
  this: JsonSupport with DatabaseConnectionProvider with SettingsProvider with AuthenticationProvider =>

  val event_findRoute = path(JavaUUID / "event" / JavaUUID) {
    (serviceId, eventId) =>
      authorize(_ => true) {
        implicit iu =>
          get {
            futureComplete {
              runSql(c =>
                new EventRequest(settings, c).getEvent(serviceId, eventId))
            }
          }
      }
  }
}
