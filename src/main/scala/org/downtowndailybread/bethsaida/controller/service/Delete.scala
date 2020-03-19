package org.downtowndailybread.bethsaida.controller.service

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.request.ServiceRequest
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}

trait Delete extends ControllerBase {
  this: JsonSupport with AuthenticationProvider with DatabaseConnectionProvider with SettingsProvider =>

  val service_deleteRoute = path(JavaUUID / "delete") {
    id =>
      authorize(_.admin) {
        implicit user =>
          post {
            futureComplete({
              runSql(c => new ServiceRequest(settings, c).deleteService(id))
              "service has been deleted"
            })
          }
      }
  }
}
