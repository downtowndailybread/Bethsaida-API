package org.downtowndailybread.bethsaida.controller.authentication

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.exception.auth.ResetTokenInvalidException
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.parameters.PasswordResetParameters
import org.downtowndailybread.bethsaida.request.UserRequest
import org.downtowndailybread.bethsaida.request.util.DatabaseSource
import org.downtowndailybread.bethsaida.providers.AuthenticationProvider

trait ResetPassword extends ControllerBase {
  this: JsonSupport with AuthenticationProvider =>

  val auth_resetPasswordPath = path("resetPassword") {
    post {
      authorize(_ => true) {
        implicit iu =>
        entity(as[PasswordResetParameters]) {
          params =>
            futureComplete(
              DatabaseSource.runSql {
                conn =>
                  val userRequest = new UserRequest(conn)
                  userRequest.emailAndTokenMatch(params.email, params.token) match {
                    case Some(user) => userRequest.updateUser(user.getUserParameters(params.password))
                    case None => throw new ResetTokenInvalidException()
                  }
              }
            )
        }
      }
    }
  }
}