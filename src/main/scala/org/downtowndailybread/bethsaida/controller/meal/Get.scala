package org.downtowndailybread.bethsaida.controller.meal

import java.time.{LocalDate, LocalDateTime}
import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.{LocalDateTimeWrapper, LockerDetails}
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}
import org.downtowndailybread.bethsaida.request.{LockerRequest, MailRequest, MealRequest}

import java.time.format.DateTimeFormatter

trait Get extends ControllerBase {
  this: JsonSupport with DatabaseConnectionProvider with SettingsProvider with AuthenticationProvider =>

  val meal_getRoute = path(Segment) {
    dateString =>
      authorizeNotAnonymous {
        implicit iu =>
          get {
            futureComplete {
              runSql(c => {
                val date = LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE).atStartOfDay()
                new MealRequest(settings, c).getMeal(date)
              }
              )
            }
          }
      }
  }
}
