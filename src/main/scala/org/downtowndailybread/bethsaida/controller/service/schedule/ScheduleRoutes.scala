package org.downtowndailybread.bethsaida.controller.service.schedule

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.service.AuthenticationProvider

trait ScheduleRoutes extends Delete with New with Update {
  this: JsonSupport with AuthenticationProvider =>

  val scheduleRoutes = schedule_newRoute ~
    schedule_updateRoute ~
    schedule_deleteRoute
}
