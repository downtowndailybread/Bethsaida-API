package org.downtowndailybread.bethsaida.controller.authentication

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers.PathEnd
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.parameters.LoginParameters
import org.downtowndailybread.bethsaida.request.AuthRequest
import org.downtowndailybread.bethsaida.request.util.DatabaseSource
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, SettingsProvider}
import spray.json.{JsObject, JsString}

trait Login {
  this: AuthenticationProvider
    with JsonSupport
    with SettingsProvider =>

  val auth_loginRoute = path(PathEnd) {
    post {
      entity(as[LoginParameters]) {
        params =>
          val user = DatabaseSource.runSql(conn => new AuthRequest(conn, settings).getUser(params))
          val authToken = createSignedToken(user.id)
          complete(JsObject(Map(("auth_token", JsString(authToken)))))
      }
    }
  }
}
