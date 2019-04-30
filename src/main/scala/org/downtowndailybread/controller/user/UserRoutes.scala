package org.downtowndailybread.controller.user

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.controller.authentication.Login
import org.downtowndailybread.json.JsonSupport
import org.downtowndailybread.service.SecretProvider

trait UserRoutes {
  this: JsonSupport
    with SecretProvider =>

  val allUserRoutes = pathPrefix("user") {
    complete("a")
  }
}
