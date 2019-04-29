package org.downtowndailybread.bethsaida.controller.clientattributetype

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.request.{ClientAttributeTypeRequest, DatabaseSource}

trait Delete {
  this: JsonSupport =>

  val clientAttributeType_deleteRoute = {
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
