package org.downtowndailybread.bethsaida.controller.service.event

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.EventAttribute
import org.downtowndailybread.bethsaida.request.EventRequest
import org.downtowndailybread.bethsaida.request.util.DatabaseSource
import org.downtowndailybread.bethsaida.service.{AuthenticationProvider, SettingsProvider}

trait New extends ControllerBase {
  this: JsonSupport with SettingsProvider with AuthenticationProvider =>

  val event_newRoute = path(JavaUUID / "event" / "new") {
    (serviceId) =>
      authorize(_ => true) {
        implicit iu =>
          post {
            entity(as[EventAttribute]) {
              ea =>
                futureComplete {
                  DatabaseSource.runSql(conn =>
                    new EventRequest(conn, settings).createEvent(serviceId, ea))
                }
            }
          }
      }
  }
}
