package org.downtowndailybread.bethsaida.controller.service.event

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}

trait EventRoutes extends All with Delete with Find with New with Update {
  this: JsonSupport with AuthenticationProvider with SettingsProvider with DatabaseConnectionProvider =>

  val eventRoutes = event_allRoute ~
    event_deleteRoute ~
    event_findRoute ~
    event_newRoute ~
    event_updateRoute

}
