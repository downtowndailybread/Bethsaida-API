package org.downtowndailybread.bethsaida.controller.authentication

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.ConfirmEmail
import org.downtowndailybread.bethsaida.request.AuthRequest
import org.downtowndailybread.bethsaida.request.util.DatabaseSource
import org.downtowndailybread.bethsaida.service.{AuthenticationProvider, SettingsProvider}

trait Confirm {
  this: AuthenticationProvider with JsonSupport with SettingsProvider =>

  val auth_confirmRoute = {
    path("confirm") {
      authorizeNotAnonymous {
        implicit authUser =>
          post {
            entity(as[ConfirmEmail]) {
              conf =>
                DatabaseSource.runSql(conn => new AuthRequest(settings, conn).confirmUser(conf))
                complete("user confirmed")
            }
          }
      }
    }
  }
}
