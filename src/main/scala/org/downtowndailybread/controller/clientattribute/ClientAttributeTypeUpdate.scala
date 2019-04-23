package org.downtowndailybread.controller.clientattribute


import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.ApiGlobalResources
import org.downtowndailybread.json.JsonSupport
import org.downtowndailybread.model.ClientAttributeType
import org.downtowndailybread.request.{ClientAttributeTypeRequest, DatabaseSource}

class ClientAttributeTypeUpdate extends JsonSupport with ApiGlobalResources {

  def updateClientAttributeTypeRoute() = {
    path(JavaUUID / "update") {
      id =>
        post {
          entity(as[ClientAttributeType]) {
            cat =>
              DatabaseSource.runSql(c => new ClientAttributeTypeRequest(c).updateClientAttributeType(
                id, cat, true))
              complete {
                DatabaseSource.runSql(c => new ClientAttributeTypeRequest(c).getClientAttributeTypes().find {
                  cat => cat.id == id
                }.map(_.tpe))
              }
          }
        }
    }
  }
}
