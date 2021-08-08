package org.downtowndailybread.bethsaida.controller.client

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.{MergeClients, UpsertClient}
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}
import org.downtowndailybread.bethsaida.request.ClientRequest


trait Merge extends ControllerBase {
  this: JsonSupport
    with AuthenticationProvider
    with SettingsProvider
    with DatabaseConnectionProvider =>

  val client_mergeRoute =
    path("merge") {
      post {
        authorize(iu => iu.admin) {
          implicit user =>
            entity(as[MergeClients]) {
              attribs =>
                futureCompleteCreated(runSql(c => new ClientRequest(settings, c).mergeClients(attribs)))
            }
        }
      }
    }
}
