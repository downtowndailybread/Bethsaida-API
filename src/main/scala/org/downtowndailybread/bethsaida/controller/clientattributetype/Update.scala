package org.downtowndailybread.bethsaida.controller.clientattributetype

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.exception.clientattributetype.ClientAttributeTypeNotFoundException
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
                      val req = new ClientAttributeTypeRequest(settings, c)
                      req.getClientAttributeTypes().find(_.id == attribName) match {
                        case Some(attrib) => req.updateClientAttributeType(
                          attrib.copy(clientAttributeTypeAttribute = cat)
                        )
                        case None => throw new ClientAttributeTypeNotFoundException(attribName)
                      }
                    }
                    "client updated"
                  }
              }
            }
        }
    }
  }
}
