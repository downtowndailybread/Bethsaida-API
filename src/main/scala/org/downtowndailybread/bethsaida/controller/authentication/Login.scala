package org.downtowndailybread.bethsaida.controller.authentication

import akka.http.scaladsl.server.Directives.{as, complete, entity, path, post}
import akka.http.scaladsl.server.PathMatchers.PathEnd
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.downtowndailybread.bethsaida.exception.client.IncorrectPasswordException
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.LoginParameters
import org.downtowndailybread.bethsaida.request.{AuthRequest, DatabaseSource, UserRequest}
import org.downtowndailybread.bethsaida.service.SecretProvider
import spray.json.{JsObject, JsString}

trait Login {

  this: JsonSupport
    with SecretProvider =>

  val auth_loginRoute = path(PathEnd) {
    post {
      entity(as[LoginParameters]) {
        params =>
          val user = DatabaseSource.runSql(conn => new AuthRequest(conn).getUser(params))
          val authToken = JWT.create()
            .withClaim("iss", s"bethsaida")
            .withClaim("sub", user.id.toString)
            .sign(Algorithm.HMAC256(secret))
          complete(JsObject(Map(("auth_token", JsString(authToken)))))

      }
    }
  }
}
