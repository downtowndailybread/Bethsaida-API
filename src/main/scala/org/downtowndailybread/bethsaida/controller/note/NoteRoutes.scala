package org.downtowndailybread.bethsaida.controller.note

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, MaterializerProvider, S3Provider}

trait NoteRoutes extends Get with Put{
  this: AuthenticationProvider with JsonSupport with DatabaseConnectionProvider with MaterializerProvider with S3Provider =>

  val allNoteRoutes = pathPrefix("note") {
    note_getRoute ~ note_putRoute
  }
}


