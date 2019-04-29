package org.downtowndailybread.controller.authentication

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.json.JsonSupport
import org.downtowndailybread.service.SecretProvider

trait AuthenticationRoutes extends Confirm with Login {
  this: JsonSupport with SecretProvider =>

  val allAuthenticationRoutes = pathPrefix("authenticate") {
    auth_loginRoute ~ auth_confirmRoute
  }
}
