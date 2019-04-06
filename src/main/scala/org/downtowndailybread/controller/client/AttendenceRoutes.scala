package org.downtowndailybread.controller.client

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.model.json.JsonSupport

trait AttendenceRoutes {
  this: JsonSupport =>

  import org.downtowndailybread.FakeData._

  val attendenceRoutes = {
    path(LongNumber / "events") {
      clientId =>
        get{
          complete(allAttendence.filter(_.client.id == clientId).map(_.event))
        }
    }
  }

}
