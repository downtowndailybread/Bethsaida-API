package org.downtowndailybread.bethsaida.controller

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.authentication.AuthenticationRoutes
import org.downtowndailybread.bethsaida.controller.user.UserRoutes
import org.downtowndailybread.bethsaida.controller.client.ClientRoutes
import org.downtowndailybread.bethsaida.controller.clientattributetype.ClientAttributeTypeRoutes
import org.downtowndailybread.bethsaida.controller.service.ServiceRoutes
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, SettingsProvider}

trait ApplicationRoutes
  extends AuthenticationRoutes
    with ClientRoutes
    with ClientAttributeTypeRoutes
    with ServiceRoutes
    with UserRoutes {

  this: AuthenticationProvider with SettingsProvider with JsonSupport  =>

  val allRoutes = ignoreTrailingSlash {
    allAuthenticationRoutes ~ allUserRoutes ~ allClientRoutes ~ allClientAttributeTypeRoutes ~ allServiceRoutes
  }
}
