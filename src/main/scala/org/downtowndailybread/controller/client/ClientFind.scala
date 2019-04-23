package org.downtowndailybread.controller.client

import java.util.UUID

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.json.JsonSupport
import org.downtowndailybread.request.{ClientRequest, DatabaseSource}

class ClientFind extends JsonSupport {

  val findClientRoute = path(Segment) {
    idStr => get {
      val id = UUID.fromString(idStr)
      pathEnd {
        complete(DatabaseSource.runSql(c => new ClientRequest(c).getClientById(id)))
      }
    }
  }
}
