package org.downtowndailybread.bethsaida.controller.note

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}
import org.downtowndailybread.bethsaida.request.NoteRequest
import spray.json.{JsObject, JsString}

trait Get extends ControllerBase {
  this: JsonSupport with DatabaseConnectionProvider with SettingsProvider with AuthenticationProvider =>

  val note_getRoute = path(JavaUUID) {
    id =>
      authorizeNotAnonymous {
        implicit iu =>
          get {
            futureComplete {
              runSql(c =>
                new NoteRequest(settings, c).getNote(id) match {
                  case Some(e) => e
                  case None => JsObject(Map("nonote" -> JsString("nonote")))
                }
              )
            }
          }
      }
  }
}
