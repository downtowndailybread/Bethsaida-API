package org.downtowndailybread.bethsaida.controller.client

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.ClientAttribute
import org.downtowndailybread.bethsaida.request.{ClientRequest, DatabaseSource}
import org.downtowndailybread.bethsaida.service.AuthenticationProvider

trait Update {
  this: AuthenticationProvider with JsonSupport =>

  val client_updateRoute = path(JavaUUID / "update") {
    id =>
      authorizeNotAnonymous  {
        implicit authUser =>
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
}