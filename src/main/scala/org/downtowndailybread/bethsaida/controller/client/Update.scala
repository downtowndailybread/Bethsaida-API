package org.downtowndailybread.bethsaida.controller.client

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.ClientAttribute
import org.downtowndailybread.bethsaida.request.ClientRequest
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}

trait Update extends ControllerBase {
  this: AuthenticationProvider with JsonSupport with SettingsProvider with DatabaseConnectionProvider =>

  val client_updateRoute = path(JavaUUID / "update") {
    id =>
      authorizeNotAnonymous {
        implicit authUser =>
          post {
            entity(as[Seq[ClientAttribute]]) {
              attribs => {
                runSql(c => new ClientRequest(settings, c).updateClient(id, attribs))
                complete(runSql(c => new ClientRequest(settings, c).getClientById(id)))
              }
            }
          }
      }
  }
}