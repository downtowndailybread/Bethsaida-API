package org.downtowndailybread.controller.client

import java.util.UUID

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.json.JsonSupport
import org.downtowndailybread.model.Success
import org.downtowndailybread.request.{ClientRequest, DatabaseSource}

trait Delete {
  this: JsonSupport =>

  val client_deleteRoute = path(Segment / "delete") {
    idStr =>
      val id = UUID.fromString(idStr)
      post {
        DatabaseSource.runSql(c => new ClientRequest(c).deleteClient(id))
        complete(Success(s"client id $id successfully deleted"))
      }
  }
}
