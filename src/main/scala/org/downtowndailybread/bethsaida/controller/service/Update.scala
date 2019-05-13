package org.downtowndailybread.bethsaida.controller.service

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.ServiceAttributes
import org.downtowndailybread.bethsaida.request.ServiceRequest
import org.downtowndailybread.bethsaida.request.util.DatabaseSource
import org.downtowndailybread.bethsaida.providers.AuthenticationProvider

trait Update extends ControllerBase {
  this: JsonSupport with AuthenticationProvider =>

  val service_updateRoute = path(JavaUUID / "update") {
    id =>
      authorizeNotAnonymous {
        implicit user =>
          post {
            entity(as[ServiceAttributes]) {
              sa =>
                futureComplete({
                  DatabaseSource.runSql(c => new ServiceRequest(c, settings).updateService(id, sa))
                  "service updated"
                })
            }
          }
      }
  }
}
