package org.downtowndailybread.bethsaida.controller.note

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.Note
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}
import org.downtowndailybread.bethsaida.request.NoteRequest

trait Put extends ControllerBase {
  this: JsonSupport with DatabaseConnectionProvider with SettingsProvider with AuthenticationProvider =>

  val note_putRoute = path(JavaUUID) {
    id =>
      authorizeNotAnonymous {
        implicit iu =>
          post {
            entity(as[Note]) {
              note =>
              futureComplete {
                runSql { c =>
                  new NoteRequest(settings, c).setNote(id, note)
                  note
                }
              }
            }
          }
      }
  }
}
