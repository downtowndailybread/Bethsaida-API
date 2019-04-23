package org.downtowndailybread.controller.client

import java.util.UUID

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.json.JsonSupport
import org.downtowndailybread.model.Success
import org.downtowndailybread.request.{ClientRequest, DatabaseSource}

class ClientDelete extends JsonSupport {

  val deleteClientRoute = path(Segment / "delete") {
    idStr =>
      val id = UUID.fromString(idStr)
      post {
        DatabaseSource.runSql(c => new ClientRequest(c).updateClient(id, Seq()))
        complete(Success(s"client id $id successfully deleted"))
      }
  }
}
