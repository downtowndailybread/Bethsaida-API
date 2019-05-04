package org.downtowndailybread.bethsaida.controller.service

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.request.ServiceRequest
import org.downtowndailybread.bethsaida.request.util.DatabaseSource
import org.downtowndailybread.bethsaida.service.AuthenticationProvider

trait Delete extends ControllerBase {
  this: JsonSupport with AuthenticationProvider =>

  val service_deleteRoute = path(JavaUUID / "delete") {
    id =>
      authorizeNotAnonymous {
        implicit user =>
          post {
            futureComplete({
              DatabaseSource.runSql(c => new ServiceRequest(c).deleteService(id))
              "service has been deleted"
            })
          }
      }
  }
}
