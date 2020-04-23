package org.downtowndailybread.bethsaida.controller.locker

import java.time.LocalDateTime

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.{LocalDateTimeWrapper, LockerDetails}
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}
import org.downtowndailybread.bethsaida.request.LockerRequest

trait Remove extends ControllerBase {
  this: JsonSupport with DatabaseConnectionProvider with SettingsProvider with AuthenticationProvider =>

  val locker_removeRoute = path(JavaUUID) {
    id =>
      authorizeNotAnonymous {
        implicit iu =>
          post {
            entity(as[LocalDateTimeWrapper]) {
              lockerDetail =>
                futureCompleteCreated {
                  runSql { c =>
                    new LockerRequest(settings, c).removeLocker(id, lockerDetail.date)
                  }
                }
            }
          }
      }
  }
}
