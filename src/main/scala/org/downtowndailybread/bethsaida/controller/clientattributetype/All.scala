package org.downtowndailybread.bethsaida.controller.clientattributetype

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.request.ClientAttributeTypeRequest
import org.downtowndailybread.bethsaida.request.util.DatabaseSource

trait All {
  this: JsonSupport =>

  val clientAttributeType_allRoute =
    path(PathEnd) {
      get {
        complete(DatabaseSource.runSql(c =>
          new ClientAttributeTypeRequest(c).getClientAttributeTypes())
        )
      }
    }
}
