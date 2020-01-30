package org.downtowndailybread.bethsaida.controller.service.schedule

import akka.http.scaladsl.server.Directives.{JavaUUID, as, entity, path, post}
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.exception.service.ScheduleNotFoundException
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.ScheduleDetail
import org.downtowndailybread.bethsaida.request.{ScheduleRequest, ServiceRequest}
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}

trait Update extends ControllerBase {
  this: JsonSupport with AuthenticationProvider with DatabaseConnectionProvider with SettingsProvider =>

  val schedule_updateRoute = path(JavaUUID / "schedule" / JavaUUID / "update") {
    (serviceId, scheduleId) =>
      authorizeNotAnonymous {
        implicit user =>
          post {
            entity(as[ScheduleDetail]) {
              detail =>
                futureComplete({
                  val scheduleIds =
                    runSql(c =>
                      new ServiceRequest(settings, c).getService(serviceId)).schedules.map(_.id)
                  if(!scheduleIds.contains(scheduleId)) {
                    throw new ScheduleNotFoundException(scheduleId)
                  }
                  runSql(c => new ScheduleRequest(settings, c).updateSchedule(scheduleId, detail))
                  "schedule updated"
                })
            }
          }
      }
  }
}
