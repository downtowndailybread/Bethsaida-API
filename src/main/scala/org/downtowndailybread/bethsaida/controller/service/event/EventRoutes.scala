package org.downtowndailybread.bethsaida.controller.service.event

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}

trait EventRoutes extends All with Delete with Find with New with Update {
  this: JsonSupport with AuthenticationProvider with SettingsProvider with DatabaseConnectionProvider =>

  val eventRoutes = pathPrefix(JavaUUID / "event") {
    serviceId =>
      event_allRoute(serviceId) ~
        event_deleteRoute(serviceId) ~
        event_findRoute(serviceId) ~
        event_newRoute(serviceId) ~
        event_updateRoute(serviceId)
  }
}
