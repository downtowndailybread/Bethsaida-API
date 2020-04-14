package org.downtowndailybread.bethsaida.controller.event

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}
import org.downtowndailybread.bethsaida.request.EventRequest

trait All extends ControllerBase {
  this: JsonSupport
    with AuthenticationProvider
    with DatabaseConnectionProvider
    with SettingsProvider =>

//  val event_allForServiceRoute = (serviceId: UUID) => path(PathEnd) {
//    authorize(_ => true) {
//      implicit iu =>
//        get {
//          futureComplete(runSql(c =>
//            new EventRequest(settings, c).getAllServiceEvents(serviceId)))
//        }
//    }
//  }

  val event_allRoute = path(PathEnd) {
    authorizeNotAnonymous {
      implicit iu =>
        get {
          futureComplete(runSql(c =>
            new EventRequest(settings, c).getAllEvents()))
        }
    }
  }

  val event_allActiveRoute = path("active") {
    authorizeNotAnonymous {
      implicit iu =>
        get {
          futureComplete(runSql(c =>
            new EventRequest(settings, c).getAllActiveEvents()
          ))
        }
    }
  }
}
