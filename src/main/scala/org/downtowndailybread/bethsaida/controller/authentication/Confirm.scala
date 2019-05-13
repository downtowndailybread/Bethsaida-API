package org.downtowndailybread.bethsaida.controller.authentication

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.{AnonymousUser, ConfirmEmail}
import org.downtowndailybread.bethsaida.request.AuthRequest
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}

trait Confirm {
  this: AuthenticationProvider with JsonSupport with SettingsProvider with DatabaseConnectionProvider =>

  val auth_confirmRoute = {
    authorize(_ == AnonymousUser) {
      implicit iu =>
        path("confirm") {
          post {
            entity(as[ConfirmEmail]) {
              conf =>
                runSql(conn => new AuthRequest(settings, conn).confirmUser(conf))
                complete("user confirmed")
            }
          }
        }
    }
  }
}