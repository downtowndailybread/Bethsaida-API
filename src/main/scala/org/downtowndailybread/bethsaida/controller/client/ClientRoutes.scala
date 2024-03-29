package org.downtowndailybread.bethsaida.controller.client

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.client.ban.BanRoutes
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, MaterializerProvider, S3Provider}

trait ClientRoutes
extends All with Delete with Find with New with Update with ImageUpload with ImageDelete with BanRoutes with Merge{
  this: AuthenticationProvider with JsonSupport with DatabaseConnectionProvider with MaterializerProvider with S3Provider =>

  val allClientRoutes = pathPrefix("client") {
    client_newRoute ~ client_findRoute ~ client_deleteRoute ~ client_updateRoute ~
      client_allRoute ~ client_imageuploadRoute ~ client_imagedeleteRoute ~ client_banRoutes ~ client_mergeRoute

  }
}
