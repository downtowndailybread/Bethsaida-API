package org.downtowndailybread.bethsaida.controller.service.schedule

import akka.http.scaladsl.server.Directives.{JavaUUID, path}
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.exception.service.ScheduleNotFoundException
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.request.ServiceRequest
import org.downtowndailybread.bethsaida.request.util.DatabaseSource
import org.downtowndailybread.bethsaida.providers.AuthenticationProvider

trait Delete extends ControllerBase {
  this: JsonSupport with AuthenticationProvider =>

  val schedule_deleteRoute = path(JavaUUID / "schedule" / JavaUUID / "delete") {
    (serviceId, scheduleId) =>
      authorizeNotAnonymous {
        implicit user =>
          futureComplete({
            val scheduleIds =
              DatabaseSource.runSql(c => new ServiceRequest(c).getService(serviceId)).schedules.map(_.id)
            if (!scheduleIds.contains(scheduleId)) {
              throw new ScheduleNotFoundException(scheduleId)
            }
            DatabaseSource.runSql(c => new ServiceRequest(c).deleteSchedule(scheduleId))
            "schedule deleted"
          })
      }
  }
}
