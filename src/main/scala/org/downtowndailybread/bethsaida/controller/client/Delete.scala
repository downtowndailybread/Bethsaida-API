package org.downtowndailybread.bethsaida.controller.client

import java.util.UUID

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.Success
import org.downtowndailybread.bethsaida.request.ClientRequest
import org.downtowndailybread.bethsaida.providers._

trait Delete extends ControllerBase {
  this: JsonSupport with AuthenticationProvider with SettingsProvider with DatabaseConnectionProvider =>

  val client_deleteRoute = path(Segment / "delete") {
    idStr =>
      val id = UUID.fromString(idStr)
      post {
        authorize(_.admin) {
          implicit authUser =>
            runSql(c => new ClientRequest(settings, c).deleteClient(id))
            complete(Success(s"client id $id successfully deleted"))
        }
      }
  }
}
