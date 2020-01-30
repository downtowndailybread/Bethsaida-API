package org.downtowndailybread.bethsaida.controller.clientattributetype

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}

trait ClientAttributeTypeRoutes
extends All with New with Update with Delete {
  this: AuthenticationProvider with JsonSupport with SettingsProvider with DatabaseConnectionProvider =>

  val allClientAttributeTypeRoutes = pathPrefix("clientAttributeType") {
    clientAttributeType_allRoute ~
      clientAttributeType_newRoute ~
      clientAttributeType_updateRoute ~
      clientAttributeType_deleteRoute
  }
}
