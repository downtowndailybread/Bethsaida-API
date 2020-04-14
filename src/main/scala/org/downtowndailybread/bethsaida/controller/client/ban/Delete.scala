package org.downtowndailybread.bethsaida.controller.client.ban

import akka.http.scaladsl.server.Directives.{JavaUUID, path, post}
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}
import org.downtowndailybread.bethsaida.request.BanRequest

trait Delete extends ControllerBase {
  this: AuthenticationProvider with JsonSupport with SettingsProvider with DatabaseConnectionProvider =>


  val client_deleteBan = path(JavaUUID / "ban" / "delete") {
    uuid =>
      authorizeNotAnonymous {
        implicit iu =>
          post {
            futureComplete(
              runSql { c =>
                new BanRequest(settings, c).deleteBan(uuid)
                "complete"
              }
            )
          }
      }
  }
}
