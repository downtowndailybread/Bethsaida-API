package org.downtowndailybread.bethsaida.controller.client.ban

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.BanAttribute
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}
import org.downtowndailybread.bethsaida.request.BanRequest

trait New extends ControllerBase {
  this: AuthenticationProvider with JsonSupport with SettingsProvider with DatabaseConnectionProvider =>

  val client_newBan = path(JavaUUID / "ban") {
    uuid =>
      authorizeNotAnonymous {
        implicit iu =>
          post {
            entity(as[BanAttribute]) {
              ba =>
                futureCompleteCreated(
                  runSql { c =>
                    val br = new BanRequest(settings, c)
                    br.deleteBan(uuid)
                    br.insertBan(uuid, ba)
                  }
                )
            }
          }
      }
  }
}
