package org.downtowndailybread.bethsaida.controller.service.schedule

import akka.http.scaladsl.server.Directives.{JavaUUID, path}
import org.downtowndailybread.bethsaida.controller.Directives.futureComplete
import org.downtowndailybread.bethsaida.exception.service.ScheduleNotFoundException
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.request.ServiceRequest
import org.downtowndailybread.bethsaida.request.util.DatabaseSource
import org.downtowndailybread.bethsaida.service.AuthenticationProvider

trait Delete {
  this: JsonSupport with AuthenticationProvider =>

  val schedule_deleteRoute = path(JavaUUID / "schedule" / JavaUUID / "delete") {
    (serviceId, scheduleId) =>
      authorizeNotAnonymous {
        implicit user =>
          futureComplete({
            val schedIds =
              DatabaseSource.runSql(c => new ServiceRequest(c).getService(serviceId)).schedules.map(_.id)
            if (!schedIds.contains(scheduleId)) {
              throw new ScheduleNotFoundException(scheduleId)
            }
            DatabaseSource.runSql(c => new ServiceRequest(c).deleteSchedule(scheduleId))
            "schedule deleted"
          })
      }
  }
}
