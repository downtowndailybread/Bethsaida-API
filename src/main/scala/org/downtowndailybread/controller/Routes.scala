package org.downtowndailybread.controller

import org.downtowndailybread.controller.client.ClientRoutes
import org.downtowndailybread.controller.service.ServiceRoutes
import org.downtowndailybread.json.JsonSupport
import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.controller.event.EventRoutes

trait Routes
  extends ClientRoutes
{
  this: JsonSupport =>

  val routes = clientRoutes
}
