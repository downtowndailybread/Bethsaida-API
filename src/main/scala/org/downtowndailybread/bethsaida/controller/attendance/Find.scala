package org.downtowndailybread.bethsaida.controller.attendance

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}
import org.downtowndailybread.bethsaida.request.AttendanceRequest

trait Find  extends ControllerBase {
  this: JsonSupport with DatabaseConnectionProvider with SettingsProvider with AuthenticationProvider =>

  val attendance_findByEventRoute = path("event" / JavaUUID) {
    eventId =>
      authorizeNotAnonymous {
        implicit iu =>
          get {
            futureComplete {
              runSql(c =>
                new AttendanceRequest(settings, c).getAttendanceByEventId(eventId)
              )
            }
          }
      }
  }
}
