package org.downtowndailybread.controller.client

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.json.JsonSupport
import org.downtowndailybread.model.ClientAttribute
import org.downtowndailybread.request.{ClientRequest, DatabaseSource}

class ClientUpdate extends JsonSupport {

  val updateClientRoute = path(JavaUUID / "update") {
    id =>
      post {
        entity(as[Seq[ClientAttribute]]) {
          attribs => {
            DatabaseSource.runSql(c => new ClientRequest(c).updateClient(id, attribs))
            complete(DatabaseSource.runSql(c => new ClientRequest(c).getClientById(id)))
          }
        }
      }
  }
}
