package org.downtowndailybread.bethsaida.controller.client

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport

trait ClientRoutes
extends All with Delete with Find with New with Update {
  this: JsonSupport =>

  val allClientRoutes = pathPrefix("client") {
    client_newRoute ~ client_findRoute ~ client_deleteRoute ~ client_updateRoute ~ client_allRoute
  }
}
