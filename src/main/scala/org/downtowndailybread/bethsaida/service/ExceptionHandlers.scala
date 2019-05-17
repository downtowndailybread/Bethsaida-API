package org.downtowndailybread.bethsaida.service

import akka.http.scaladsl.model.StatusCodes.{BadRequest, NotFound, Unauthorized}
import akka.http.scaladsl.server.Directives.{complete, extractUri}
import akka.http.scaladsl.server.ExceptionHandler
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import org.downtowndailybread.bethsaida.exception.{DDBException, NotFoundException, UnauthorizedException}
import org.downtowndailybread.bethsaida.json.JsonSupport

object ExceptionHandlers extends JsonSupport {
  val exceptionHandlers = ExceptionHandler {
    case r: NotFoundException =>
      extractUri {
        uri =>
          cors() {
            complete((NotFound, ddbExceptionFormat.write(r)))
          }
      }
    case r: UnauthorizedException =>
      extractUri {
        uri =>
          cors() {
            complete((Unauthorized, ddbExceptionFormat.write(r)))
          }
      }
    case r: DDBException =>
      extractUri {
        uri =>
          cors() {
            complete((BadRequest, ddbExceptionFormat.write(r)))
          }
      }
  }

}
