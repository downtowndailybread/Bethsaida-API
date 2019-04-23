package org.downtowndailybread.controller.clientattributetype

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.json.JsonSupport
import org.downtowndailybread.request.{ClientAttributeTypeRequest, DatabaseSource}

class ClientAttributeTypeDelete extends JsonSupport {

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
