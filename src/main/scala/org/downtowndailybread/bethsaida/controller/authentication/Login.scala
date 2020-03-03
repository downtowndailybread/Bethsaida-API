package org.downtowndailybread.bethsaida.controller.authentication

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers.PathEnd
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.parameters.LoginParameters
import org.downtowndailybread.bethsaida.request.AuthRequest
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}
import spray.json.{JsBoolean, JsObject, JsString}

trait Login extends ControllerBase {
  this: AuthenticationProvider
    with JsonSupport
    with SettingsProvider
    with DatabaseConnectionProvider =>

  val auth_loginRoute = path(PathEnd) {
    post {
      entity(as[LoginParameters]) {
        params =>
          val user = runSql(conn => new AuthRequest(settings, conn).getUser(params))
          val authToken = createSignedToken(user.id)
          futureComplete {
            Thread.sleep(3000)
            JsObject(Map(
              "auth_token" -> JsString(authToken),
              "admin" -> JsBoolean(user.admin)
            ))
          }
      }
    }
  }
}
