package org.downtowndailybread.controller.clientattribute

import akka.http.scaladsl.server.Directives.{complete, get, path, pathPrefix}
import org.downtowndailybread.json.JsonSupport
import org.downtowndailybread.request.{ClientAttributeTypeRequest, DatabaseSource}

trait ClientAttributeRoutes {
  this: JsonSupport =>

  val clientAttributesRoutes =
    pathPrefix("clientAttributeTypes") {
      path("") {
        get {
          complete(DatabaseSource.runSql(c => new ClientAttributeTypeRequest(c).getClientAttributeTypes()).map(_._2))
        }
      } //~
//      path("upsert") {
//        post {
//          entity(as[Seq[ClientAttributeType]]) {
//            cats =>
//              ClientAttributeTypeRequest.insertClientAttributeType(cats)
//              complete(ClientAttributeTypeRequest.getClientAttributeTypes())
//          }
//        }
//      } ~
//        path("delete") {
//          post {
//            entity(as[Seq[ClientAttributeType]]) {
//              cats =>
//                ClientAttributeTypeRequest.deleteClientAttributeType(cats)
//                complete(ClientAttributeTypeRequest.getClientAttributeTypes())
//            }
//          }
//        }
    }
}
