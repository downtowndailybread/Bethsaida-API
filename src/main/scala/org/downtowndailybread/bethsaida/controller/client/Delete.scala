package org.downtowndailybread.bethsaida.controller.client

import java.util.UUID

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.Success
import org.downtowndailybread.bethsaida.request.{ClientRequest, DatabaseSource}
import org.downtowndailybread.bethsaida.service.AuthenticationProvider

trait Delete {
  this: JsonSupport with AuthenticationProvider =>

  val client_deleteRoute = path(Segment / "delete") {
    idStr =>
      val id = UUID.fromString(idStr)
      post {
        authorizeNotAnonymous {
          implicit authUser =>
            DatabaseSource.runSql(c => new ClientRequest(c).deleteClient(id))
            complete(Success(s"client id $id successfully deleted"))
        }
      }
  }
}
