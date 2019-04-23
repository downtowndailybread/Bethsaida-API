package org.downtowndailybread.controller.clientattributetype

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.ApiGlobalResources
import org.downtowndailybread.exceptions.clientattributetype.ClientAttributeTypeInsertionErrorException
import org.downtowndailybread.json.JsonSupport
import org.downtowndailybread.model.{ClientAttributeType, ClientAttributeTypeInternal}
import org.downtowndailybread.request.{ClientAttributeTypeRequest, DatabaseSource}

class ClientAttributeTypeDelete extends JsonSupport with ApiGlobalResources {

  def deleteClientAttributeTypeRoute() = {
    path(Segment / "delete") {
      attribName =>
        post {
          val record = DatabaseSource.runSql(conn =>
            new ClientAttributeTypeRequest(conn).deleteClientAttributeType(attribName: String))
          complete(StatusCodes.OK)
        }
    }
  }
}
