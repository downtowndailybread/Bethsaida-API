package org.downtowndailybread.bethsaida.controller.authentication

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.ConfirmEmail
import org.downtowndailybread.bethsaida.request.UserRequest
import org.downtowndailybread.bethsaida.request.util.DatabaseSource
import spray.json.JsBoolean

trait ValidateToken extends ControllerBase {
  this: JsonSupport =>

  val auth_validateTokenPath = path("validateToken") {
    post {
      entity(as[ConfirmEmail]) {
        conf =>
          futureComplete(JsBoolean(DatabaseSource.runSql(c =>
            new UserRequest(c, settings).emailAndTokenMatch(conf.email, conf.token).isDefined)))
      }
    }
  }
}
