package org.downtowndailybread.controller.client

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.FakeData
import org.downtowndailybread.model.exceptions.NoSuchClientException
import org.downtowndailybread.model.json.JsonSupport

trait ClientRoutes extends AttendenceRoutes {
  this: JsonSupport =>

  import FakeData._

  val clientRoutes = {
    pathPrefix("client") {
      path("") {
        get {
          complete(allClients)
        }
      } ~
      path(LongNumber) { id =>
        allClients.find(_.metadata.id == id) match {
          case Some(c) => complete(c)
          case None =>
            throw new NoSuchClientException(id)
        }
      } ~
      attendenceRoutes
    }
  }

}
