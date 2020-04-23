package org.downtowndailybread.bethsaida.controller.locker

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}
import org.downtowndailybread.bethsaida.request.{LockerRequest, NoteRequest}
import spray.json.{JsObject, JsString}

trait All extends ControllerBase {
  this: JsonSupport with DatabaseConnectionProvider with SettingsProvider with AuthenticationProvider =>

  val locker_allRoute = path(PathEnd) {
    authorizeNotAnonymous {
      implicit iu =>
        get {
          futureComplete {
            runSql(c =>
              new LockerRequest(settings, c).getLockers()
            )
          }
        }
    }
  }
}
