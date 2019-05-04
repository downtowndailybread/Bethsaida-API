package org.downtowndailybread.bethsaida.controller.service

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.ServiceAttributes
import org.downtowndailybread.bethsaida.request.ServiceRequest
import org.downtowndailybread.bethsaida.request.util.DatabaseSource
import org.downtowndailybread.bethsaida.service.AuthenticationProvider

trait Find extends ControllerBase {
  this: JsonSupport with AuthenticationProvider =>

  val service_findRoute = path(JavaUUID) {
    id =>
      authorizeNotAnonymous {
        implicit user =>
          get {
            futureComplete(DatabaseSource.runSql(c => new ServiceRequest(c).getService(id)))
          }
      }
  }
}
