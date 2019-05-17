package org.downtowndailybread.bethsaida.controller.service.schedule

import akka.http.scaladsl.server.Directives.{JavaUUID, as, entity, path, post}
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.exception.service.ScheduleNotFoundException
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.ScheduleDetail
import org.downtowndailybread.bethsaida.request.ServiceRequest
import org.downtowndailybread.bethsaida.request.util.DatabaseSource
import org.downtowndailybread.bethsaida.providers.AuthenticationProvider

trait Update extends ControllerBase {
  this: JsonSupport with AuthenticationProvider =>

  val schedule_updateRoute = path(JavaUUID / "schedule" / JavaUUID / "update") {
    (serviceId, scheduleId) =>
      authorizeNotAnonymous {
        implicit user =>
          post {
            entity(as[ScheduleDetail]) {
              detail =>
                futureComplete({
                  val scheduleIds =
                    DatabaseSource.runSql(c => new ServiceRequest(c).getService(serviceId)).schedules.map(_.id)
                  if(!scheduleIds.contains(scheduleId)) {
                    throw new ScheduleNotFoundException(scheduleId)
                  }
                  DatabaseSource.runSql(c => new ServiceRequest(c).updateSchedule(scheduleId, detail))
                  "schedule updated"
                })
            }
          }
      }
  }
}
