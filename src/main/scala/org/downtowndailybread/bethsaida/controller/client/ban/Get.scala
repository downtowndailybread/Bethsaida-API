package org.downtowndailybread.bethsaida.controller.client.ban

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}
import org.downtowndailybread.bethsaida.request.BanRequest
import spray.json.{JsObject, JsValue}

trait Get extends ControllerBase {
  this: AuthenticationProvider with JsonSupport with SettingsProvider with DatabaseConnectionProvider =>

  val client_getBan = path(JavaUUID / "ban") {
    uuid =>
      authorizeNotAnonymous {
        implicit iu => get {
          futureComplete(
            runSql{c =>
              val ban = new BanRequest(settings, c).getBanFromClientId(uuid)
              if(ban.isEmpty) {
                JsObject(Map[String, JsValue]())
              } else {
                ban
              }
            }
          )
        }
      }
  }
}
