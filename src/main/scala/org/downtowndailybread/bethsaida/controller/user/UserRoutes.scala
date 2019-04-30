package org.downtowndailybread.bethsaida.controller.user

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.authentication.Login
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.service.SecretProvider

trait UserRoutes {
  this: JsonSupport
    with SecretProvider =>

  val allUserRoutes = pathPrefix("user") {
    complete("a")
  }
}
