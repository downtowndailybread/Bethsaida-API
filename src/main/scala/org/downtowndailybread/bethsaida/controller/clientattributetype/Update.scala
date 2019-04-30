package org.downtowndailybread.bethsaida.controller.clientattributetype

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.{ClientAttributeType, ClientAttributeTypeAttribute}
import org.downtowndailybread.bethsaida.request.{ClientAttributeTypeRequest, DatabaseSource}

trait Update {
  this: JsonSupport =>

  val clientAttributeType_updateRoute = {
    path(Segment / "update") {
      attribName =>
        post {
          entity(as[ClientAttributeTypeAttribute]) {
            cat =>
              DatabaseSource.runSql { c =>
                new ClientAttributeTypeRequest(c)
                  .updateClientAttributeType(ClientAttributeType(attribName, cat), true)
              }
              complete {
                DatabaseSource.runSql(c => new ClientAttributeTypeRequest(c).getClientAttributeTypes())
              }
          }
        }
    }
  }
}
