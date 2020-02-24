package org.downtowndailybread.bethsaida.controller.client

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.UpsertClient
import org.downtowndailybread.bethsaida.request.ClientRequest
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}


trait New extends ControllerBase {
  this: JsonSupport
    with AuthenticationProvider
    with SettingsProvider
    with DatabaseConnectionProvider =>

  val client_newRoute =
    path("new") {
      post {
        authorizeNotAnonymous {
          implicit user =>
            entity(as[UpsertClient]) {
              attribs =>
                futureCompleteCreated(runSql(c => new ClientRequest(settings, c).insertClient(attribs)))
            }
        }
      }
    }
}
