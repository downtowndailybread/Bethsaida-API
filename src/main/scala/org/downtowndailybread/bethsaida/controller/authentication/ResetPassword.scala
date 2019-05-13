package org.downtowndailybread.bethsaida.controller.authentication

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.exception.auth.ResetTokenInvalidException
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.parameters.PasswordResetParameters
import org.downtowndailybread.bethsaida.request.UserRequest
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}

trait ResetPassword extends ControllerBase {
  this: JsonSupport with AuthenticationProvider with SettingsProvider with DatabaseConnectionProvider =>

  val auth_resetPasswordRoute = path("resetPassword") {
    post {
      authorize(_ => true) {
        implicit iu =>
          entity(as[PasswordResetParameters]) {
            params =>
              futureComplete(
                runSql {
                  c =>
                    val userRequest = new UserRequest(settings, c)
                    userRequest.emailAndTokenMatch(params.email, params.token) match {
                      case Some(user) => userRequest.updateUserFromReset(user.getUserParameters(params.password))
                      case None => throw new ResetTokenInvalidException()
                    }
                }
              )
          }
      }
    }
  }
}