package org.downtowndailybread.controller.clientattributetype

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.json.JsonSupport
import org.downtowndailybread.model.ClientAttributeType
import org.downtowndailybread.request.{ClientAttributeTypeRequest, DatabaseSource}

trait New {
  this: JsonSupport =>

  val clientAttributeType_newRoute = {
    path("new") {
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
