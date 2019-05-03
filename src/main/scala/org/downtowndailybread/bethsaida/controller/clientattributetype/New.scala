package org.downtowndailybread.bethsaida.controller.clientattributetype

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.ClientAttributeType
import org.downtowndailybread.bethsaida.request.ClientAttributeTypeRequest
import org.downtowndailybread.bethsaida.request.util.DatabaseSource
import org.downtowndailybread.bethsaida.service.AuthenticationProvider

trait New {
  this: AuthenticationProvider with JsonSupport =>

  val clientAttributeType_newRoute = {
    path("new") {
      authorizeNotAnonymous {
        implicit authUser =>
          post {
            entity(as[Seq[ClientAttributeType]]) {
              cats =>
                cats.foreach { cat =>
                  DatabaseSource.runSql(c =>
                    new ClientAttributeTypeRequest(c).insertClientAttributeType(cat))
                }
                complete(StatusCodes.Created)
            }
          }
      }
    }
  }
}
