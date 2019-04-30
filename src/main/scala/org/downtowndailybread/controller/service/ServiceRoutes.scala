package org.downtowndailybread.controller.service

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.json.JsonSupport
import spray.json.JsNumber

trait ServiceRoutes {
  this: JsonSupport =>


  val allServiceRoutes = {
    pathPrefix("service") {
      path("") {
        get {
          complete(JsNumber(1))
        }
      }
    }
  }
}
