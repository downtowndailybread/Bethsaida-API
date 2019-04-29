package org.downtowndailybread.controller.authentication

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.json.JsonSupport
import org.downtowndailybread.model.ConfirmEmail
import org.downtowndailybread.request.{AuthRequest, DatabaseSource}

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
