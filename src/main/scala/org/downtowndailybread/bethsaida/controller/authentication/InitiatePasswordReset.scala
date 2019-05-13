package org.downtowndailybread.bethsaida.controller.authentication

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.AnonymousUser
import org.downtowndailybread.bethsaida.model.parameters.InitiatePasswordResetParameters
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}
import org.downtowndailybread.bethsaida.request.UserRequest

trait InitiatePasswordReset extends ControllerBase {
  this: JsonSupport with SettingsProvider with DatabaseConnectionProvider with AuthenticationProvider =>

  val auth_initiatePasswordResetRoute = path("initiatePasswordReset") {
    post {
      authorize(r => r == AnonymousUser) {
        implicit u =>
          entity(as[InitiatePasswordResetParameters]) {
            conf =>
              futureComplete(runSql { c =>
                new UserRequest(settings, c).intiatePasswordReset(conf.email)
                "password reset started"
              })
          }
      }
    }
  }
}
