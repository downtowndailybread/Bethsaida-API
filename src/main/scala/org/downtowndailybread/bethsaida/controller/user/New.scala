package org.downtowndailybread.bethsaida.controller.user

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.parameters.UserParameters
import org.downtowndailybread.bethsaida.request.UserRequest
import org.downtowndailybread.bethsaida.request.util.DatabaseSource
import org.downtowndailybread.bethsaida.providers.AuthenticationProvider

trait New extends ControllerBase{
  this: AuthenticationProvider with JsonSupport =>

  val user_newRoute = path("new") {
    authorizeNotAnonymous {
      implicit authUser =>
        post {
          entity(as[UserParameters]) {
            us =>
              futureCompleteCreated(DatabaseSource.runSql(conn => new UserRequest(conn).insertUser(us)))
          }
        }
    }
  }
}
