package org.downtowndailybread

import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.server.Directives._

trait ApiGlobalResources {

  val version = "v1"

  val port = 8090


  def generateUri(uriEnding: String): Uri = {
    Uri.apply(s"api/${version}/$uriEnding")
  }

  val apiPathPrefix = "api" / version

}
