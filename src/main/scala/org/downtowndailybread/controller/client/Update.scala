package org.downtowndailybread.controller.client

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.json.JsonSupport
import org.downtowndailybread.model.ClientAttribute
import org.downtowndailybread.request.{ClientRequest, DatabaseSource}

trait Update {
  this: JsonSupport =>

  val client_updateRoute = path(JavaUUID / "update") {
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
