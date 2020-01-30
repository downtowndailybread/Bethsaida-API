package org.downtowndailybread.bethsaida.controller.client

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, MaterializerProvider}

trait ClientRoutes
extends All with Delete with Find with New with Update with ImageUpload {
  this: AuthenticationProvider with JsonSupport with DatabaseConnectionProvider with MaterializerProvider =>

  val allClientRoutes = pathPrefix("client") {
    client_newRoute ~ client_findRoute ~ client_deleteRoute ~ client_updateRoute ~
      client_allRoute ~ client_imageuploadroute

  }
}
