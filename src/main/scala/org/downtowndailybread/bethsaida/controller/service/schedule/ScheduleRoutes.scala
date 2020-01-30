package org.downtowndailybread.bethsaida.controller.service.schedule

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}

trait ScheduleRoutes extends Delete with New with Update {
  this: JsonSupport with AuthenticationProvider with SettingsProvider with DatabaseConnectionProvider =>

  val scheduleRoutes = schedule_newRoute ~
    schedule_updateRoute ~
    schedule_deleteRoute
}
