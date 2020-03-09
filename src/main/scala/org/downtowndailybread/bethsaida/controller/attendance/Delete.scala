package org.downtowndailybread.bethsaida.controller.attendance

import java.util.UUID

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}
import org.downtowndailybread.bethsaida.request.{AttendanceRequest, EventRequest}

trait Delete extends ControllerBase {
  this: JsonSupport with DatabaseConnectionProvider with SettingsProvider with AuthenticationProvider =>

  val attendance_deleteRoute = path(JavaUUID / "delete") {
    attendanceId =>
      authorizeNotAnonymous {
        implicit iu =>
          post {
            futureCompleteCreated {
              runSql(c =>
                new AttendanceRequest(settings, c).deleteAttendance(attendanceId)
              )
              attendanceId
            }
          }
      }
  }
}
