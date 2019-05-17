package org.downtowndailybread.bethsaida.controller.service

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.service.{AuthenticationProvider, SettingsProvider}

trait ServiceRoutes
  extends All
    with New
    with Find
    with Update
    with Delete
    with schedule.ScheduleRoutes
    with event.EventRoutes {
  this: JsonSupport with AuthenticationProvider with SettingsProvider =>

  val allServiceRoutes = {
    pathPrefix("service") {

      val serviceRoutes = service_allRoute ~
        service_newRoute ~
        service_findRoute ~
        service_updateRoute ~
        service_deleteRoute


      serviceRoutes ~ scheduleRoutes ~ eventRoutes
    }
  }
}
