package org.downtowndailybread.bethsaida.controller.user

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.parameters.UserParameters
import org.downtowndailybread.bethsaida.request.UserRequest
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}

trait Update extends ControllerBase {
  this: AuthenticationProvider
    with JsonSupport
    with DatabaseConnectionProvider
    with SettingsProvider =>

  val user_updateRoute = path(JavaUUID / "update") {
    uid =>
      authorize(_.id == uid) {
        implicit authUser =>
          post {
            entity(as[UserParameters]) {
              u =>
                futureComplete(runSql(c =>
                  new UserRequest(settings, c).updateUser(u, uid)))
            }
          }
      }
  }
}
