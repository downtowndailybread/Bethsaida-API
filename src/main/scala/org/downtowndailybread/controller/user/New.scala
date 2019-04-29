package org.downtowndailybread.controller.user

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.json.JsonSupport
import org.downtowndailybread.model.NewUserParameters
import org.downtowndailybread.request.{DatabaseSource, UserRequest}
import org.downtowndailybread.service.SecretProvider

trait New {
  this: JsonSupport with SecretProvider =>

  val user_newRoute = path("new") {
      post {
        entity(as[NewUserParameters]) {
          us =>
            DatabaseSource.runSql(conn => new UserRequest(conn).insertClient(us))
            complete((StatusCodes.Created, s"user with email ${us.loginParameters.email} created"))
        }
      }
    }


}
