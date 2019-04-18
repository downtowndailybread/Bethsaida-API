package org.downtowndailybread.controller.client

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.json.JsonSupport
import spray.json.JsNumber

trait AttendenceRoutes {
  this: JsonSupport =>

  val attendenceRoutes = {
    path(LongNumber / "events") {
      clientId =>
        get{
//          complete(allAttendence.filter(_.client.id == clientId).map(_.event))
          complete(JsNumber(1))
        }
    }
  }

}
