package org.downtowndailybread.bethsaida.controller.service.schedule

import akka.http.scaladsl.server.Directives.{JavaUUID, path}
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.exception.service.ScheduleNotFoundException
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.request.ServiceRequest
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}

trait Delete extends ControllerBase {
  this: JsonSupport with AuthenticationProvider with DatabaseConnectionProvider with SettingsProvider =>

  val schedule_deleteRoute = path(JavaUUID / "schedule" / JavaUUID / "delete") {
    (serviceId, scheduleId) =>
      authorizeNotAnonymous {
        implicit user =>
          futureComplete({
            val scheduleIds =
              runSql(c => new ServiceRequest(settings, c).getService(serviceId)).schedules.map(_.id)
            if (!scheduleIds.contains(scheduleId)) {
              throw new ScheduleNotFoundException(scheduleId)
            }
            runSql(c => new ServiceRequest(settings, c).deleteSchedule(scheduleId))
            "schedule deleted"
          })
      }
  }
}
