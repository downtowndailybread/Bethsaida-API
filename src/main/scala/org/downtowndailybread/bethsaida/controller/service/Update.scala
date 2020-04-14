package org.downtowndailybread.bethsaida.controller.service

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.ServiceAttributes
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}
import org.downtowndailybread.bethsaida.request.ServiceRequest

trait Update extends ControllerBase {
  this: JsonSupport with AuthenticationProvider with DatabaseConnectionProvider with SettingsProvider =>

  val service_updateRoute = path(JavaUUID / "update") {
    id =>
      authorize(_.admin) {
        implicit user =>
          post {
            entity(as[ServiceAttributes]) {
              sa =>
                futureCompleteCreated({
                  runSql(c => new ServiceRequest(settings, c).updateService(id, sa))
                  id
                })
            }
          }
      }
  }
}
