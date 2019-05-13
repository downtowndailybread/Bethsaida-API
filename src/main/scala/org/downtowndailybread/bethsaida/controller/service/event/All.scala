package org.downtowndailybread.bethsaida.controller.service.event

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.request.EventRequest
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}

trait All extends ControllerBase {
  this: JsonSupport
    with AuthenticationProvider
    with DatabaseConnectionProvider
    with SettingsProvider =>

  val event_allRoute = path(JavaUUID / "event") {
    serviceId =>
      authorize(_ => true) {
        implicit iu =>
          get {
            futureComplete(runSql(c =>
              new EventRequest(settings, c).getAllServiceEvents(serviceId)))
          }
      }
  }
}
