package org.downtowndailybread.bethsaida.controller.clientattributetype

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.request.ClientAttributeTypeRequest
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}

trait Delete extends ControllerBase {
  this: AuthenticationProvider with JsonSupport with SettingsProvider with DatabaseConnectionProvider =>

  val clientAttributeType_deleteRoute = {
    path(Segment / "delete") {
      attribName =>
        authorizeNotAnonymous {
          implicit authUser =>
            post {
              futureComplete {
                runSql(c =>
                  new ClientAttributeTypeRequest(settings, c).deleteClientAttributeType(attribName: String))
                StatusCodes.OK
              }
            }
        }
    }
  }
}
