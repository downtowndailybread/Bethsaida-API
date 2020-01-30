package org.downtowndailybread.bethsaida.controller.client

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.request.ClientRequest
import org.downtowndailybread.bethsaida.providers._

trait All extends ControllerBase {
  this: JsonSupport with AuthenticationProvider with SettingsProvider with DatabaseConnectionProvider =>

  val client_allRoute = path(PathEnd) {
    authorizeNotAnonymous {
      implicit user =>
        get {
          futureComplete(runSql(c => new ClientRequest(settings, c).getClients()))
        }

    }
  }
}
