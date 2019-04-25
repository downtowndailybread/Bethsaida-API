package org.downtowndailybread.controller.event

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.json.JsonSupport
import spray.json.JsNumber

trait EventRoutes {
  this: JsonSupport =>


  val eventRoutes = {
    pathPrefix("event") {
      path("") {
        get {
//          complete(allEvents)
          complete(JsNumber(1))
        }
      } /*~
      path(LongNumber) { id =>
        allEvents.find(_.metadata.id == id) match {
          case Some(c) => complete(c)
          case None =>
            throw new NoSuchEventException(id)
        }
      }*/
    }
  }
}
