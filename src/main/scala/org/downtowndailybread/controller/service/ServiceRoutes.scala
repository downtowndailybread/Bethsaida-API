package org.downtowndailybread.controller.service

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.json.JsonSupport
import spray.json.JsNumber

trait ServiceRoutes {
  this: JsonSupport =>


  val serviceRoutes = {
    pathPrefix("service") {
      path("") {
        get {
//          complete(allServices)
          complete(JsNumber(1))
        }
      } /*~
      path(LongNumber) {
        eventId =>
          allServices.find(_.id == eventId) match {
            case Some(e) => complete(e)
            case None => throw new NoSuchServiceException(eventId)
          }
      }*/
    }
  }
}
