package org.downtowndailybread.bethsaida.controller.client

import java.util.UUID

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}
import org.downtowndailybread.bethsaida.request.ClientRequest

trait Find extends ControllerBase {
  this: JsonSupport with AuthenticationProvider with SettingsProvider with DatabaseConnectionProvider =>

  val client_findRoute = path(Segment) {
    idStr =>
      authorizeNotAnonymous {
        implicit user =>
          get {
            val id = UUID.fromString(idStr)
            pathEnd {
              complete(runSql(c =>
                new ClientRequest(settings, c).getClientById(id)))
            }
          }
      }
  }
}
