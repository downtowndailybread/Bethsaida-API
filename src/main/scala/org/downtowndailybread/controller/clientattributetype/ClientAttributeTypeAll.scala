package org.downtowndailybread.controller.clientattributetype

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.json.JsonSupport
import org.downtowndailybread.request.{ClientAttributeTypeRequest, DatabaseSource}

class ClientAttributeTypeAll extends JsonSupport {

  def allClientAttributeTypeRoute() =
    path(PathEnd) {
      get {
        complete(DatabaseSource.runSql(c =>
          new ClientAttributeTypeRequest(c).getClientAttributeTypes())
        )
      }
    }
}
