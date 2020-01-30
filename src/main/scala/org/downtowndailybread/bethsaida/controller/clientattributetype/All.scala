package org.downtowndailybread.bethsaida.controller.clientattributetype

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}
import org.downtowndailybread.bethsaida.request.ClientAttributeTypeRequest

trait All extends ControllerBase {
  this: JsonSupport with SettingsProvider with DatabaseConnectionProvider with AuthenticationProvider =>

  val clientAttributeType_allRoute =
    path(PathEnd) {
      get {
        authorizeNotAnonymous {
          implicit iu =>
          futureComplete(runSql(c =>
            new ClientAttributeTypeRequest(settings, c).getClientAttributeTypes())
          )
        }
      }
    }
}
