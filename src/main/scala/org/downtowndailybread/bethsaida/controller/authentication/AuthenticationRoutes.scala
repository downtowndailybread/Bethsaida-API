package org.downtowndailybread.bethsaida.controller.authentication

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.service.SecretProvider

trait AuthenticationRoutes extends Confirm with Login {
  this: JsonSupport with SecretProvider =>

  val allAuthenticationRoutes = pathPrefix("authenticate") {
    auth_loginRoute ~ auth_confirmRoute
  }
}
