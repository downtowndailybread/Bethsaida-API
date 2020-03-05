package org.downtowndailybread.bethsaida.controller.user

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.parameters.UserParameters
import org.downtowndailybread.bethsaida.request.UserRequest
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}

trait New extends ControllerBase {
  this: AuthenticationProvider
    with JsonSupport
    with DatabaseConnectionProvider
    with SettingsProvider =>

  val user_newRoute = path("new") {
    authorize(_.admin) {
      implicit authUser =>
        post {
          entity(as[UserParameters]) {
            us =>
              futureCompleteCreated(runSql(c => new UserRequest(settings, c).insertUser(us)))
          }
        }
    }
  }
}
