package org.downtowndailybread.controller.service

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.FakeData
import org.downtowndailybread.model.exceptions.NoSuchServiceException
import org.downtowndailybread.model.json.JsonSupport

trait ServiceRoutes {
  this: JsonSupport =>

  import FakeData._

  val serviceRoutes = {
    pathPrefix("service") {
      path("") {
        get {
          complete(allServices)
        }
      } ~
      path(LongNumber) {
        eventId =>
          allServices.find(_.id == eventId) match {
            case Some(e) => complete(e)
            case None => throw new NoSuchServiceException(eventId)
          }
      }
    }
  }
}
