package org.downtowndailybread.bethsaida.controller.authentication

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.service.{AuthenticationProvider, SettingsProvider}

trait AuthenticationRoutes extends Confirm with Login {
  this: AuthenticationProvider with JsonSupport with SettingsProvider =>

  val allAuthenticationRoutes = pathPrefix("authenticate") {
    auth_loginRoute ~ auth_confirmRoute
  }
}
