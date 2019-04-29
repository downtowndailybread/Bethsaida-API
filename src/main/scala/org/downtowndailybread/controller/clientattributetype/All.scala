package org.downtowndailybread.controller.clientattributetype

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.json.JsonSupport
import org.downtowndailybread.request.{ClientAttributeTypeRequest, DatabaseSource}

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
