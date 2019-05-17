package org.downtowndailybread.bethsaida.controller.service.event

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.service.{AuthenticationProvider, SettingsProvider}

trait EventRoutes extends All with Delete with Find with New with Update {
  this: JsonSupport with AuthenticationProvider with SettingsProvider =>

  val eventRoutes = event_allRoute ~
    event_deleteRoute ~
    event_findRoute ~
    event_newRoute ~
    event_updateRoute

}
