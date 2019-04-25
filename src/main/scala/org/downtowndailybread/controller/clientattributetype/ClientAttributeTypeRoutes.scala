package org.downtowndailybread.controller.clientattributetype

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.json.JsonSupport

trait ClientAttributeTypeRoutes {
  this: JsonSupport =>

  val allClientAttributeTypeRoute = new ClientAttributeTypeAll().allClientAttributeTypeRoute()
  val newClientAttributeTypeRoute = new ClientAttributeTypeNew().newClientAttributeTypeRoute()
  val updateClientAttributeTypeRoute = new ClientAttributeTypeUpdate().updateClientAttributeTypeRoute()
  val deleteClientAttributeTypeRoute = new ClientAttributeTypeDelete().deleteClientAttributeTypeRoute()

  val allClientAttributeTypeRoutes = pathPrefix("clientAttributeType") {
    allClientAttributeTypeRoute ~
      newClientAttributeTypeRoute ~
      updateClientAttributeTypeRoute ~
      deleteClientAttributeTypeRoute
  }
}
