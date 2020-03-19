package org.downtowndailybread.bethsaida.controller.client.ban

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}

trait BanRoutes extends Get with New with Delete {
  this: AuthenticationProvider with JsonSupport with SettingsProvider with DatabaseConnectionProvider =>

  val client_banRoutes = client_getBan ~ client_newBan ~ client_deleteBan
}
