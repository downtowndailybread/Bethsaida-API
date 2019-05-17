package org.downtowndailybread.bethsaida.controller.service

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.ServiceAttributes
import org.downtowndailybread.bethsaida.request.ServiceRequest
import org.downtowndailybread.bethsaida.request.util.DatabaseSource
import org.downtowndailybread.bethsaida.providers.AuthenticationProvider

trait New extends ControllerBase {
  this: JsonSupport with AuthenticationProvider =>

  val service_newRoute = path("new") {
    authorizeNotAnonymous {
      implicit user =>
        post {
          entity(as[ServiceAttributes]) {
            attrib =>
              futureComplete(DatabaseSource.runSql(c => new ServiceRequest(c).insertService(attrib)))
          }
        }
    }
  }
}
