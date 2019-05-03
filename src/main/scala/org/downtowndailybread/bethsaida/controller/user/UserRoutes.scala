package org.downtowndailybread.bethsaida.controller.user

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.service.AuthenticationProvider

trait UserRoutes extends New {
  this: AuthenticationProvider with JsonSupport =>

  val allUserRoutes = pathPrefix("user") {
    user_newRoute
  }
}
