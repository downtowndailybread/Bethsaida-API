package org.downtowndailybread.bethsaida.controller.event

import java.util.UUID

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}
import org.downtowndailybread.bethsaida.request.EventRequest

trait Delete extends ControllerBase {
  this: JsonSupport with DatabaseConnectionProvider with SettingsProvider with AuthenticationProvider =>

  val event_deleteRoute = (serviceId: UUID) => path(JavaUUID / "delete") {
    eventId =>
      authorizeNotAnonymous {
        implicit iu =>
          post {
            futureComplete {
              runSql(c =>
                new EventRequest(settings, c).deleteEvent(serviceId, eventId))
              "event deleted"
            }
          }
      }
  }
}
