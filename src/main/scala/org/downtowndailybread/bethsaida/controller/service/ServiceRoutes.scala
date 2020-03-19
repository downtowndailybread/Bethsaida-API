package org.downtowndailybread.bethsaida.controller.service

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}

trait ServiceRoutes
  extends All
    with New
    with Find
    with Update
    with Delete
    with org.downtowndailybread.bethsaida.controller.event.EventRoutes {
  this: JsonSupport with AuthenticationProvider with SettingsProvider with DatabaseConnectionProvider =>

  val allServiceRoutes = {
    pathPrefix("service") {

      val serviceRoutes =
        service_allRoute ~
          service_newRoute ~
          service_findRoute ~
          service_updateRoute ~
          service_deleteRoute


      serviceRoutes
    }
  }
}
