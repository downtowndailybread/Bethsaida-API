package org.downtowndailybread.bethsaida.controller.event

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.{ServiceType, ServiceTypeObj}
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}
import org.downtowndailybread.bethsaida.request.EventRequest

trait All extends ControllerBase {
  this: JsonSupport
    with AuthenticationProvider
    with DatabaseConnectionProvider
    with SettingsProvider =>


  val event_allRoute = path(ServiceTypeObj / PathEnd) {
    serviceType => {
      authorizeNotAnonymous {
        implicit iu =>
          get {
            futureComplete(runSql(c =>
              new EventRequest(settings, c).getAllEvents(serviceType)))
          }
      }
    }
  }

  val event_allActiveRoute = path("active" / ServiceTypeObj) {
    serviceType => {
      authorizeNotAnonymous {
        implicit iu =>
          get {
            futureComplete(runSql(c =>
              new EventRequest(settings, c).getAllActiveEvents(serviceType)
            ))
          }
      }
    }
  }
}
