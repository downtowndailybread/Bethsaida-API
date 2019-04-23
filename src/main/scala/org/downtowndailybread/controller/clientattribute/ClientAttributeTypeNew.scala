package org.downtowndailybread.controller.clientattribute

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.ApiGlobalResources
import org.downtowndailybread.json.JsonSupport
import org.downtowndailybread.model.ClientAttributeType
import org.downtowndailybread.request.{ClientAttributeTypeRequest, DatabaseSource}

class ClientAttributeTypeNew extends JsonSupport with ApiGlobalResources {

  def newClientAttributeTypeRoute() = {
    path("new") {
      post {
        entity(as[Seq[ClientAttributeType]]) {
          cats =>
            val newAttribIds = cats.map { cat =>
              DatabaseSource.runSql(c => new ClientAttributeTypeRequest(c).newClientAttributeType(cat))
            }
            complete {
              DatabaseSource.runSql(c => new ClientAttributeTypeRequest(c).getClientAttributeTypes().filter {
                cat => newAttribIds.contains(cat.id)
              }.map(_.tpe))
            }
        }
      }
    }
  }
}
