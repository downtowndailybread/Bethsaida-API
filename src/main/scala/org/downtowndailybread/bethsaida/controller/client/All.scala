package org.downtowndailybread.bethsaida.controller.client

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.request.ClientRequest
import org.downtowndailybread.bethsaida.service.AuthenticationProvider
import org.downtowndailybread.bethsaida.request.util.DatabaseSource

trait All extends ControllerBase {
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
