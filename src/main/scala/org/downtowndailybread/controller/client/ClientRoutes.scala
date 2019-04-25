package org.downtowndailybread.controller.client

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.json.JsonSupport


trait ClientRoutes {
  this: JsonSupport =>

  private val newClientRoute = new ClientNew().newClientRoute
  private val deleteClientRoute = new ClientDelete().deleteClientRoute
  private val updateClientRoute = new ClientUpdate().updateClientRoute
  private val findClientRoute = new ClientFind().findClientRoute
  private val allClientsRoute = new ClientAll().allClientRoute

  val allClientRoutes = pathPrefix("client") {
    newClientRoute ~ findClientRoute ~ deleteClientRoute ~ updateClientRoute ~ allClientsRoute
  }
}
