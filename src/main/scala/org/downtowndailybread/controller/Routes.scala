package org.downtowndailybread.controller

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.controller.client.ClientRoutes
import org.downtowndailybread.controller.clientattributetype.ClientAttributeTypeRoutes
import org.downtowndailybread.json.JsonSupport

trait Routes
  extends ClientRoutes with ClientAttributeTypeRoutes {
  this: JsonSupport =>

  val routes = ignoreTrailingSlash {
    allClientRoutes ~ allClientAttributeTypeRoutes
  }
}
