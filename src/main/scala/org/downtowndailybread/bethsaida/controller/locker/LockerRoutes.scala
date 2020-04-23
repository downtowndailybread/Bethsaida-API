package org.downtowndailybread.bethsaida.controller.locker

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, MaterializerProvider, S3Provider}

trait LockerRoutes extends All with Put with Remove {
  this: AuthenticationProvider with JsonSupport with DatabaseConnectionProvider with MaterializerProvider with S3Provider =>

  val allLockerRoutes = pathPrefix("locker") {
    locker_allRoute ~ locker_putRoute ~ locker_removeRoute
  }
}


