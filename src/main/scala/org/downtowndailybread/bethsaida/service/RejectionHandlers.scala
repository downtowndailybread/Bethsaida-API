package org.downtowndailybread.bethsaida.service

import akka.http.scaladsl.model.StatusCodes.{BadRequest, Unauthorized}
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.{AuthenticationFailedRejection, MalformedRequestContentRejection, Rejection, Route}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import org.downtowndailybread.bethsaida.exception.{MalformedJsonErrorException, UnauthorizedException}

object RejectionHandlers {

  def rejectionHanders(corsSettings: CorsSettings): PartialFunction[Rejection, Route] = {
    case MalformedRequestContentRejection(msg, e) ⇒ {
      val rejectionMessage = "The request content was malformed: " + msg
      cors(corsSettings) {
        complete(BadRequest, throw new MalformedJsonErrorException(rejectionMessage))
      }
    }
    case AuthenticationFailedRejection(msg, _) => {
      cors(corsSettings) {
        complete(Unauthorized, new UnauthorizedException())
      }
    }
  }
}
