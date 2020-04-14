package org.downtowndailybread.bethsaida.controller.event

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.EventAttribute
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}
import org.downtowndailybread.bethsaida.request.EventRequest

trait Update extends ControllerBase {
  this: JsonSupport with DatabaseConnectionProvider with SettingsProvider with AuthenticationProvider =>

  val event_updateRoute = path(JavaUUID / "update") {
    eventId =>
      authorizeNotAnonymous {
        implicit iu =>
          post {
            entity(as[EventAttribute]) {
              ea =>
                futureCompleteCreated {
                  runSql(c =>
                    new EventRequest(settings, c).updateEvent(eventId, ea))
                }
            }
          }
      }
  }
}
