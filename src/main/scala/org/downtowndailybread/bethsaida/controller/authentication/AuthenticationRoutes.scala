package org.downtowndailybread.bethsaida.controller.authentication

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}

trait AuthenticationRoutes extends Confirm with Login with InitiatePasswordReset with ResetPassword {
  this: AuthenticationProvider with JsonSupport with SettingsProvider with DatabaseConnectionProvider =>

  val allAuthenticationRoutes = pathPrefix("authenticate") {
    auth_loginRoute ~ auth_confirmRoute ~ auth_initiatePasswordResetRoute ~ auth_resetPasswordRoute
  }
}
