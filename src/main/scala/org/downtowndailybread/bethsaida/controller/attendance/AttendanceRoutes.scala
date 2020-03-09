package org.downtowndailybread.bethsaida.controller.attendance

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Directives.pathPrefix
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}

trait AttendanceRoutes extends Delete with New with Update with Find with ControllerBase {
  this: JsonSupport with AuthenticationProvider with SettingsProvider with DatabaseConnectionProvider =>


  val allAttendanceRoutes = pathPrefix("attendance") {
    attendance_findByEventRoute ~ attendance_deleteRoute ~ attendance_newRoute ~ attendance_updateRoute
  }

}
