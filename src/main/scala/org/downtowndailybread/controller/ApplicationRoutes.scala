package org.downtowndailybread.controller

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.controller.authentication.AuthenticationRoutes
import org.downtowndailybread.controller.user.UserRoutes
import org.downtowndailybread.controller.client.ClientRoutes
import org.downtowndailybread.controller.clientattributetype.ClientAttributeTypeRoutes
import org.downtowndailybread.controller.service.ServiceRoutes
import org.downtowndailybread.json.JsonSupport
import org.downtowndailybread.service.SecretProvider

trait ApplicationRoutes
  extends AuthenticationRoutes
    with ClientRoutes
    with ClientAttributeTypeRoutes
    with ServiceRoutes
    with UserRoutes {

  this: JsonSupport with SecretProvider =>

  val allRoutes = ignoreTrailingSlash {
    allAuthenticationRoutes ~ allUserRoutes ~ allClientRoutes ~ allClientAttributeTypeRoutes ~ allServiceRoutes
  }
}
