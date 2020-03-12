package org.downtowndailybread.bethsaida.controller.user

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.client.ban.BanRoutes
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}

trait UserRoutes extends New with All with Delete with Find with Update {
  this: AuthenticationProvider with JsonSupport with SettingsProvider with DatabaseConnectionProvider =>

  val allUserRoutes = pathPrefix("user") {
    user_newRoute ~ user_allRoute ~ user_deleteRoute ~ user_findRoute ~ user_updateRoute
  }
}
