package org.downtowndailybread.bethsaida.controller.user

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.parameters.UserParameters
import org.downtowndailybread.bethsaida.request.UserRequest
import org.downtowndailybread.bethsaida.request.util.DatabaseSource
import org.downtowndailybread.bethsaida.providers.AuthenticationProvider

trait Update extends ControllerBase {
  this: AuthenticationProvider with JsonSupport =>

  val user_updateRoute = path(JavaUUID / "update") {
    uid =>
      authorizeNotAnonymous {
        implicit authUser =>
          post {
            entity(as[UserParameters]) {
              u =>
                futureComplete(DatabaseSource.runSql(conn => new UserRequest(conn).updateUser(u)))
            }
          }
      }
  }
}
