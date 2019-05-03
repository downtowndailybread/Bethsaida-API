package org.downtowndailybread.bethsaida.controller.service

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.request.ServiceRequest
import org.downtowndailybread.bethsaida.service.AuthenticationProvider
import org.downtowndailybread.bethsaida.controller.Directives._
import org.downtowndailybread.bethsaida.request.util.DatabaseSource

trait All {
  this: JsonSupport with AuthenticationProvider =>

  val service_allRoute = path(PathEnd) {
    authorizeNotAnonymous {
      implicit user =>
        get {
          futureComplete(DatabaseSource.runSql(c => new ServiceRequest(c).getAllServices()))
        }
    }
  }
}