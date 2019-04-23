package org.downtowndailybread

import akka.http.scaladsl.server.Directives._

trait ApiGlobalResources {

  val version = "v1"

  val port = 8090

  val apiPathPrefix = "api" / version

}
