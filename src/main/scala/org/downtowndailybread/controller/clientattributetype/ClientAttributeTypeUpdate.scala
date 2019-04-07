package org.downtowndailybread.controller.clientattributetype


import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.exception.clientattributetype.ClientAttributeTypeInsertionErrorException
import org.downtowndailybread.json.JsonSupport
import org.downtowndailybread.model.{ClientAttributeType, ClientAttributeTypeAttribute}
import org.downtowndailybread.request.{ClientAttributeTypeRequest, DatabaseSource}

class ClientAttributeTypeUpdate extends JsonSupport {

  def updateClientAttributeTypeRoute() = {
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
