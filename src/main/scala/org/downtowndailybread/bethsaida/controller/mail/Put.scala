package org.downtowndailybread.bethsaida.controller.mail

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.{LockerDetails, MailDetails, Note}
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}
import org.downtowndailybread.bethsaida.request.{LockerRequest, MailRequest, NoteRequest}

trait Put extends ControllerBase {
  this: JsonSupport with DatabaseConnectionProvider with SettingsProvider with AuthenticationProvider =>

  val mail_putRoute = path(PathEnd) {
    authorizeNotAnonymous {
      implicit iu =>
        post {
          entity(as[MailDetails]) {
            mailDetail =>
              futureCompleteCreated {
                runSql { c =>
                  new MailRequest(settings, c).insertMail(mailDetail)
                }
              }
          }
        }
    }
  }
}
