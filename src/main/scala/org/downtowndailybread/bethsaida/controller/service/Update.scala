package org.downtowndailybread.bethsaida.controller.service

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.ServiceAttributes
import org.downtowndailybread.bethsaida.request.ServiceRequest
import org.downtowndailybread.bethsaida.request.util.DatabaseSource
import org.downtowndailybread.bethsaida.service.AuthenticationProvider

trait Update {
  this: JsonSupport with AuthenticationProvider =>

  val service_updateRoute = path(JavaUUID / "update") {
    id =>
      authorizeNotAnonymous {
        implicit user =>
          post {
            entity(as[ServiceAttributes]) {
              sa =>
                futureComplete({
                  DatabaseSource.runSql(c => new ServiceRequest(c).updateService(id, sa))
                  "service updated"
                })
            }
          }
      }
  }
}
