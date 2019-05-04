package org.downtowndailybread.bethsaida.controller.client

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.ClientAttribute
import org.downtowndailybread.bethsaida.request.ClientRequest
import org.downtowndailybread.bethsaida.request.util.DatabaseSource
import org.downtowndailybread.bethsaida.service.AuthenticationProvider
import org.downtowndailybread.bethsaida.controller.Directives._


trait New {
  this: JsonSupport with AuthenticationProvider =>

  val client_newRoute =
    path("new") {
      post {
        authorizeNotAnonymous {
          implicit user =>
            entity(as[Seq[ClientAttribute]]) {
              attribs =>
                futureCompleteCreated(DatabaseSource.runSql(c => new ClientRequest(c).insertClient(attribs)))
            }
        }
      }
    }
}
