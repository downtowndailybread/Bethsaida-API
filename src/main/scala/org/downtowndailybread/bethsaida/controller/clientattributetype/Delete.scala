package org.downtowndailybread.bethsaida.controller.clientattributetype

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.request.ClientAttributeTypeRequest
import org.downtowndailybread.bethsaida.request.util.DatabaseSource
import org.downtowndailybread.bethsaida.service.AuthenticationProvider

trait Delete extends ControllerBase {
  this: AuthenticationProvider with JsonSupport =>

  val clientAttributeType_deleteRoute = {
    path(Segment / "delete") {
      attribName =>
        authorizeNotAnonymous {
          implicit authUser =>
            post {
              futureComplete {
                DatabaseSource.runSql(conn =>
                  new ClientAttributeTypeRequest(conn).deleteClientAttributeType(attribName: String))
                StatusCodes.OK
              }
            }
        }
    }
  }
}
