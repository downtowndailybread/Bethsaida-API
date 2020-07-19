package org.downtowndailybread.bethsaida.service

import akka.http.scaladsl.model.StatusCodes.{BadRequest, Unauthorized}
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.{AuthenticationFailedRejection, MalformedRequestContentRejection, Rejection, Route}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import org.downtowndailybread.bethsaida.exception.{MalformedJsonErrorException, UnauthorizedException}

object RejectionHandlers {

  val rejectionHanders: PartialFunction[Rejection, Route] = {
    case MalformedRequestContentRejection(msg, e) ⇒ {
      val rejectionMessage = "The request content was malformed: " + msg
      cors() {
        complete(BadRequest, throw new MalformedJsonErrorException(rejectionMessage))
      }
    }
    case AuthenticationFailedRejection(msg, _) => {
      cors() {
        complete(Unauthorized, new UnauthorizedException())
      }
    }
  }
}
