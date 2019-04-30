package org.downtowndailybread.bethsaida.controller.authentication

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.ConfirmEmail
import org.downtowndailybread.bethsaida.request.{AuthRequest, DatabaseSource}

trait Confirm {
  this: JsonSupport =>

  val auth_confirmRoute = {
    path("confirm") {
      post {
        entity(as[ConfirmEmail]) {
          conf =>
            DatabaseSource.runSql(conn => new AuthRequest(conn).confirmUser(conf))
            complete("user confirmed")
        }
      }
    }
  }
}
