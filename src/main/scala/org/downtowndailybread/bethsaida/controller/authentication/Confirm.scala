package org.downtowndailybread.bethsaida.controller.authentication

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.{AnonymousUser, ConfirmEmail}
import org.downtowndailybread.bethsaida.request.AuthRequest
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}

trait Confirm extends ControllerBase {
  this: AuthenticationProvider with JsonSupport with SettingsProvider with DatabaseConnectionProvider =>

  val auth_confirmRoute = {
    path("confirm") {
      post {
        entity(as[ConfirmEmail]) {
          confirm =>
            futureCompleteCreated(
              runSql(conn => new AuthRequest(settings, conn).confirmUser(confirm))

            )
        }
      }
    }
  }
}