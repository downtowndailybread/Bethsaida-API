package org.downtowndailybread.bethsaida.auth

import akka.http.scaladsl.server.directives.Credentials
import akka.http.scaladsl.server.directives.Credentials.{Missing, Provided}
import com.auth0.jwt.JWT
import org.downtowndailybread.bethsaida.model.InternalUser
import org.downtowndailybread.bethsaida.request.{DatabaseSource, UserRequest}
import org.downtowndailybread.bethsaida.service.UUIDProvider


object Authentication extends UUIDProvider {

  def authenticate(credentials: Credentials): Option[InternalUser] = {
    credentials match {
      case Missing => None
      case Provided(str) => val id = JWT.decode(str).getSubject
        DatabaseSource.runSql(conn => new UserRequest(conn).getRawUserFromUuid(parseUUID(id)))
    }

  }

}
