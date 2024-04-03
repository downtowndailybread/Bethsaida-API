package org.downtowndailybread.bethsaida.controller.meal

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.{LockerDetails, MailDetails, MealDetails, Note}
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}
import org.downtowndailybread.bethsaida.request.{LockerRequest, MailRequest, MealRequest, NoteRequest}

trait Update extends ControllerBase {
  this: JsonSupport with DatabaseConnectionProvider with SettingsProvider with AuthenticationProvider =>

  val meal_updateRoute = path(PathEnd) {
    println("abc")
    authorizeNotAnonymous {
      implicit iu =>
        post {
          println("abc");
          entity(as[MealDetails]) {
            mailDetail =>
              futureCompleteCreated {
                runSql { c =>
                  new MealRequest(settings, c).putMeal(mailDetail)
                }
              }
          }
        }

    }
  }
}
