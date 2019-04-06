package org.downtowndailybread.controller.event

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.FakeData._
import org.downtowndailybread.model.exceptions.NoSuchEventException
import org.downtowndailybread.model.json.JsonSupport

trait EventRoutes {
  this: JsonSupport =>


  val eventRoutes = {
    pathPrefix("event") {
      path("") {
        get {
          complete(allEvents)
        }
      } ~
      path(LongNumber) { id =>
        allEvents.find(_.metadata.id == id) match {
          case Some(c) => complete(c)
          case None =>
            throw new NoSuchEventException(id)
        }
      }
    }
  }
}
