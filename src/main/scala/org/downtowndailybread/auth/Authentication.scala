package org.downtowndailybread.auth

import akka.http.scaladsl.server.directives.Credentials

object Authentication {

  def authenticate(credentials: Credentials): Option[String] = Some("coming soon")

}
