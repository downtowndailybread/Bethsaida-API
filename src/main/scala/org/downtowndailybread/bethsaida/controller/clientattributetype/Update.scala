package org.downtowndailybread.bethsaida.controller.clientattributetype

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.{ClientAttributeType, ClientAttributeTypeAttribute}
import org.downtowndailybread.bethsaida.request.ClientAttributeTypeRequest
import org.downtowndailybread.bethsaida.request.util.DatabaseSource
import org.downtowndailybread.bethsaida.service.AuthenticationProvider

trait Update extends ControllerBase {
  this: AuthenticationProvider with JsonSupport =>

  val clientAttributeType_updateRoute = {
    path(Segment / "update") {
      attribName =>
        authorizeNotAnonymous {
          implicit authUser =>
            post {
              entity(as[ClientAttributeTypeAttribute]) {
                cat =>
                  futureComplete {
                    DatabaseSource.runSql { c =>
                      new ClientAttributeTypeRequest(c)
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
