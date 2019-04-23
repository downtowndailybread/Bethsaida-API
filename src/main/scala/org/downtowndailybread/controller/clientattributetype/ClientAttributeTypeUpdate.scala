package org.downtowndailybread.controller.clientattributetype


import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.exceptions.clientattributetype.ClientAttributeTypeInsertionErrorException
import org.downtowndailybread.json.JsonSupport
import org.downtowndailybread.model.{ClientAttributeType, ClientAttributeTypeInternal}
import org.downtowndailybread.request.{ClientAttributeTypeRequest, DatabaseSource}

class ClientAttributeTypeUpdate extends JsonSupport {

  def updateClientAttributeTypeRoute() = {
    path(Segment / "update") {
      attribName =>
        post {
          entity(as[ClientAttributeType]) {
            cat =>
              if (attribName != cat.name) {
                throw new ClientAttributeTypeInsertionErrorException(attribName, cat.name)
              }
              DatabaseSource.runSql { c =>
                val catReq = new ClientAttributeTypeRequest(c)
                val id = catReq.getClientAttributeTypeInternalByName(attribName).id

                catReq.getClientAttributeTypeInternalByName(attribName)
                catReq.updateClientAttributeType(ClientAttributeTypeInternal(id, cat), true)
              }
              complete {
                DatabaseSource.runSql(c => new ClientAttributeTypeRequest(c).getClientAttributeTypes()).map(_.tpe)
              }
          }
        }
    }
  }
}
