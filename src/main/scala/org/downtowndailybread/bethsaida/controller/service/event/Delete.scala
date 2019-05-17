package org.downtowndailybread.bethsaida.controller.service.event

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.request.EventRequest
import org.downtowndailybread.bethsaida.request.util.DatabaseSource
import org.downtowndailybread.bethsaida.service.{AuthenticationProvider, SettingsProvider}

trait Delete extends ControllerBase {
  this: JsonSupport with SettingsProvider with AuthenticationProvider =>

  val event_deleteRoute = path(JavaUUID / "event" / JavaUUID / "delete") {
    (serviceId, eventId) =>
      authorize(_ => true) {
        implicit iu =>
          post {
            futureComplete {
              DatabaseSource.runSql(conn =>
                new EventRequest(conn, settings).deleteEvent(serviceId, eventId))
              "event deleted"
            }
          }
      }
  }
}
