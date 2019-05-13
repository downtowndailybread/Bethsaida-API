package org.downtowndailybread.bethsaida.controller.clientattributetype

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.{ClientAttributeType, ClientAttributeTypeAttribute}
import org.downtowndailybread.bethsaida.request.ClientAttributeTypeRequest
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}

trait Update extends ControllerBase {
  this: AuthenticationProvider with JsonSupport with DatabaseConnectionProvider with SettingsProvider =>

  val clientAttributeType_updateRoute = {
    path(Segment / "update") {
      attribName =>
        authorizeNotAnonymous {
          implicit authUser =>
            post {
              entity(as[ClientAttributeTypeAttribute]) {
                cat =>
                  futureComplete {
                    runSql { c =>
                      new ClientAttributeTypeRequest(settings, c)
                        .updateClientAttributeType(ClientAttributeType(attribName, cat), true)
                    }
                    "client updated"
                  }
              }
            }
        }
    }
  }
}
