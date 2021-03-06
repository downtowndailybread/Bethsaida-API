package org.downtowndailybread.bethsaida.controller.user

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}
import org.downtowndailybread.bethsaida.request.UserRequest

trait All extends ControllerBase {
  this: AuthenticationProvider with JsonSupport with DatabaseConnectionProvider with SettingsProvider =>


  val user_allRoute = path(PathEnd) {
    authorizeNotAnonymous {
      implicit authUser =>
        get {
          futureComplete(runSql(c =>
            simpleUserSeqFormat.write(new UserRequest(settings, c).getAllUsers())
          ))
        }
    }
  }
}