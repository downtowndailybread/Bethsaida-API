package org.downtowndailybread.bethsaida.controller.event

import java.util.UUID

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.exception.event.DuplicateRecordsException
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.EventAttribute
import org.downtowndailybread.bethsaida.request.EventRequest
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}
import org.postgresql.util.PSQLException

trait New extends ControllerBase {
  this: JsonSupport with DatabaseConnectionProvider with SettingsProvider with AuthenticationProvider =>

  val event_newRoute = path("new") {
    authorizeNotAnonymous {
      implicit iu =>
        post {
          entity(as[EventAttribute]) {
            ea =>
              futureCompleteCreated {
                try {
                  runSql(c =>
                    new EventRequest(settings, c).createEvent(ea))
                }
                catch {
                  case e: PSQLException =>
                    if(e.getServerErrorMessage.getConstraint() == "unique_event_date") {
                      throw new DuplicateRecordsException()
                    }
                    throw e
                }
              }
          }
        }
    }
  }
}
