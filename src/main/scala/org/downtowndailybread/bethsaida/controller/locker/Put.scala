package org.downtowndailybread.bethsaida.controller.locker

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.{LockerDetails, Note}
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}
import org.downtowndailybread.bethsaida.request.{LockerRequest, NoteRequest}

trait Put extends ControllerBase {
  this: JsonSupport with DatabaseConnectionProvider with SettingsProvider with AuthenticationProvider =>

  val locker_putRoute = path(PathEnd) {
    authorizeNotAnonymous {
      implicit iu =>
        post {
          entity(as[LockerDetails]) {
            lockerDetail =>
              futureCompleteCreated {
                runSql { c =>
                  new LockerRequest(settings, c).insertLocker(lockerDetail)
                }
              }
          }
        }
    }
  }
}
