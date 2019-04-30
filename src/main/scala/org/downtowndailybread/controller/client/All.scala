package org.downtowndailybread.controller.client

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.json.JsonSupport
import org.downtowndailybread.request.{ClientRequest, DatabaseSource}

trait All {
  this: JsonSupport =>

  val client_allRoute = path(PathEnd) {
    get {
      complete(DatabaseSource.runSql(c => new ClientRequest(c).getAllClients()))
    }
  }

}
