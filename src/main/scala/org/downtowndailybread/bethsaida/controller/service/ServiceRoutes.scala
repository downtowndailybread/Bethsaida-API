package org.downtowndailybread.bethsaida.controller.service

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.service.AuthenticationProvider

trait ServiceRoutes
  extends All
    with New
    with Find
    with Update
    with Delete
    with schedule.New
    with schedule.Update
    with schedule.Delete {
  this: JsonSupport with AuthenticationProvider =>

  val allServiceRoutes = {
    pathPrefix("service") {
      val serviceRoutes = service_allRoute ~
        service_newRoute ~
        service_findRoute ~
        service_updateRoute ~
        service_deleteRoute

      val scheduleRoutes = schedule_newRoute ~
        schedule_updateRoute ~
        schedule_deleteRoute

      serviceRoutes ~ scheduleRoutes
    }
  }
}
