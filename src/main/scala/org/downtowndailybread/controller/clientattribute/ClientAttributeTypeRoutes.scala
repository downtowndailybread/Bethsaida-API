package org.downtowndailybread.controller.clientattribute

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.json.JsonSupport

trait ClientAttributeTypeRoutes {
  this: JsonSupport =>

  val allClientAttributeTypeRoute = new ClientAttributeTypeAll().allClientAttributeTypeRoute()
  val newClientAttributeTypeRoute = new ClientAttributeTypeNew().newClientAttributeTypeRoute()
  val updateClientAttributeTypeRoute = new ClientAttributeTypeUpdate().updateClientAttributeTypeRoute()

  val allClientAttributeTypeRoutes = pathPrefix("clientAttributeType") {
    allClientAttributeTypeRoute ~ newClientAttributeTypeRoute
  }
  //    pathPrefix("clientAttributeTypes") {
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
