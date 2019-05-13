package org.downtowndailybread.bethsaida.controller.user

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.parameters.UserParameters
import org.downtowndailybread.bethsaida.request.UserRequest
import org.downtowndailybread.bethsaida.request.util.DatabaseSource
import org.downtowndailybread.bethsaida.providers.AuthenticationProvider

trait Delete extends ControllerBase {
  this: AuthenticationProvider with JsonSupport =>

  val user_deleteRoute = path(JavaUUID / "delete") {
    uid =>
      authorizeNotAnonymous {
        implicit authUser =>
          post {
            entity(as[UserParameters]) {
              u =>
                futureComplete(DatabaseSource.runSql(conn =>
                  new UserRequest(conn, settings).deleteUser(u)))
            }
          }
      }
  }
}
