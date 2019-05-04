package org.downtowndailybread.bethsaida.controller.clientattributetype

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.request.ClientAttributeTypeRequest
import org.downtowndailybread.bethsaida.request.util.DatabaseSource

trait All extends ControllerBase {
  this: JsonSupport =>

  val clientAttributeType_allRoute =
    path(PathEnd) {
      get {
        futureComplete(DatabaseSource.runSql(c =>
          new ClientAttributeTypeRequest(c).getClientAttributeTypes())
        )
      }
    }
}
