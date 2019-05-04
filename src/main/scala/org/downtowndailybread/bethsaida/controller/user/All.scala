package org.downtowndailybread.bethsaida.controller.user

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.request.UserRequest
import org.downtowndailybread.bethsaida.request.util.DatabaseSource
import org.downtowndailybread.bethsaida.service.AuthenticationProvider

trait All extends ControllerBase {
  this: AuthenticationProvider with JsonSupport =>

  val user_allRoute = path(PathEnd) {
    authorizeNotAnonymous {
      implicit authUser =>
        get {
          futureComplete(DatabaseSource.runSql(conn => userSeqFormat.write(new UserRequest(conn).getAllUsers)))
        }
    }
  }
}