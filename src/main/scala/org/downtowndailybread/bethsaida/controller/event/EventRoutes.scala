package org.downtowndailybread.bethsaida.controller.event

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport
import spray.json.JsNumber

trait EventRoutes {
  this: JsonSupport =>


  val eventRoutes = {
    pathPrefix("event") {
      path("") {
        get {
          complete(JsNumber(1))
        }
      }
    }
  }
}
