package org.downtowndailybread.bethsaida.controller.user

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.NewUserParameters
import org.downtowndailybread.bethsaida.request.{DatabaseSource, UserRequest}
import org.downtowndailybread.bethsaida.service.AuthenticationProvider

trait New {
  this: AuthenticationProvider with JsonSupport =>

  val user_newRoute = path("new") {
    authorize {
      c => println(c); true
    } {
      implicit authUser =>
        post {
          entity(as[NewUserParameters]) {
            us =>
              DatabaseSource.runSql(conn => new UserRequest(conn).insertClient(us))
              complete((StatusCodes.Created, s"user with email ${us.loginParameters.email} created"))
          }
        }
    }
  }
}
