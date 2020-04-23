package org.downtowndailybread.bethsaida.controller.mail

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, MaterializerProvider, S3Provider}

trait MailRoutes extends All with Put with Remove {
  this: AuthenticationProvider with JsonSupport with DatabaseConnectionProvider with MaterializerProvider with S3Provider =>

  val allMailRoutes = pathPrefix("mail") {
    mail_allRoute ~ mail_putRoute ~ mail_removeRoute
  }
}


