package org.downtowndailybread.bethsaida.controller.user

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.AuthenticationProvider

trait UserRoutes extends New with All with Delete with Find with Update{
  this: AuthenticationProvider with JsonSupport =>

  val allUserRoutes = pathPrefix("user") {
    user_newRoute ~ user_allRoute ~ user_deleteRoute ~ user_findRoute ~ user_updateRoute
  }
}
