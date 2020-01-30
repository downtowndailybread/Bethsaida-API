package org.downtowndailybread.bethsaida.controller.service.event

import java.util.UUID

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.EventAttribute
import org.downtowndailybread.bethsaida.request.EventRequest
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}

trait New extends ControllerBase {
  this: JsonSupport with DatabaseConnectionProvider with SettingsProvider with AuthenticationProvider =>

  val event_newRoute = (serviceId: UUID) => path("new") {
    authorize(_ => true) {
      implicit iu =>
        post {
          entity(as[EventAttribute]) {
            ea =>
              futureComplete {
                runSql(c =>
                  new EventRequest(settings, c).createEvent(serviceId, ea))
              }
          }
        }
    }
  }
}
