package org.downtowndailybread.bethsaida.controller.attendance

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.AttendanceAttribute
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}
import org.downtowndailybread.bethsaida.request.AttendanceRequest

trait Update extends ControllerBase {
  this: JsonSupport with DatabaseConnectionProvider with SettingsProvider with AuthenticationProvider =>

  val attendance_updateRoute = path(JavaUUID / "update") {
    id =>
      authorizeNotAnonymous {
        implicit iu =>
          post {
            entity(as[AttendanceAttribute]) {
              ea =>
                futureCompleteCreated {
                  runSql(c =>
                    new AttendanceRequest(settings, c).updateAttendance(id, ea))
                  id
                }
            }
          }
      }
  }
}
