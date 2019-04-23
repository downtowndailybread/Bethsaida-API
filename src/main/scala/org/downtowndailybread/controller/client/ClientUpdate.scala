package org.downtowndailybread.controller.client

import java.util.UUID

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.json.JsonSupport
import org.downtowndailybread.model.helper.AttribNameValuePair
import org.downtowndailybread.request.{ClientRequest, DatabaseSource}

class ClientUpdate extends JsonSupport {

  val updateClientRoute = path(Segment / "update") {
    idStr =>
      post {
        entity(as[Seq[AttribNameValuePair]]) {
          attribs => {
            val id = DatabaseSource.runSql(c => new ClientRequest(c).updateClient(UUID.fromString(idStr), attribs))
            complete(DatabaseSource.runSql(c => new ClientRequest(c).getClientById(id)))
          }
        }
      }
  }
}
