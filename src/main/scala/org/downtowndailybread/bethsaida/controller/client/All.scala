package org.downtowndailybread.bethsaida.controller.client

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.request.{ClientRequest, DatabaseSource}
import org.downtowndailybread.bethsaida.service.AuthenticationProvider
import org.downtowndailybread.bethsaida.controller.Directives._

trait All {
  this: JsonSupport with AuthenticationProvider =>

  val client_allRoute = path(PathEnd) {
    authorizeNotAnonymous {
      implicit user =>
        get {
          futureComplete(DatabaseSource.runSql(c => new ClientRequest(c).getAllClients()))
        }

    }
  }
}
