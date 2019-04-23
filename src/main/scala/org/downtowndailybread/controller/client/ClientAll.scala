package org.downtowndailybread.controller.client

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.json.JsonSupport
import org.downtowndailybread.request.{ClientRequest, DatabaseSource}

class ClientAll extends JsonSupport {

  val allClientRoute = path(PathEnd) {
    get {
      complete(DatabaseSource.runSql(c => new ClientRequest(c).getAllClients()))
    }
  }

}
