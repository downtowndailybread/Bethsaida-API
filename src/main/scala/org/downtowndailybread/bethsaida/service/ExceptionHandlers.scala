package org.downtowndailybread.bethsaida.service

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.EntityStreamSizeException
import akka.http.scaladsl.model.StatusCodes.{BadRequest, NotFound, Unauthorized}
import akka.http.scaladsl.server.Directives.{complete, extractUri}
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import org.downtowndailybread.bethsaida.exception.event.DuplicateRecordsException
import org.downtowndailybread.bethsaida.exception.{DDBException, ImageSizeTooLarge, InvalidImageFormat, NotFoundException, UnauthorizedException}
import org.downtowndailybread.bethsaida.json.ExceptionJson

object ExceptionHandlers extends SprayJsonSupport with ExceptionJson {
  def pf(corsSettings: CorsSettings): PartialFunction[Throwable, Route] = {


    case r: DuplicateRecordsException => cors() {
      complete((BadRequest, ddbExceptionFormat.write(r)))
    }

    case r: NotFoundException =>
      extractUri {
        uri =>
          complete((NotFound, ddbExceptionFormat.write(r)))
      }
    case r: UnauthorizedException =>
      extractUri {
        uri =>
          complete((Unauthorized, ddbExceptionFormat.write(r)))
      }
    case r: DDBException =>
      extractUri {
        uri =>
          complete((BadRequest, ddbExceptionFormat.write(r)))
      }
    case r: InvalidImageFormat =>
      extractUri {
        uri =>
          complete((BadRequest, ddbExceptionFormat.write(r)))
      }
    case r: ImageSizeTooLarge =>
      extractUri {
        uri =>
          complete((BadRequest, ddbExceptionFormat.write(r)))
      }

    case r: EntityStreamSizeException =>
      extractUri {
        uri =>
          cors() {
            complete((BadRequest, ddbExceptionFormat.write(new ImageSizeTooLarge())))
          }
      }
    case _ =>
      extractUri {
        uri =>
          complete((BadRequest, ddbExceptionFormat.write(new DDBException("Unknown error") {})))
      }
  }

  def exceptionHandlers(corsSettings: CorsSettings) = ExceptionHandler({
    case s: Throwable => cors(corsSettings) {
      pf(corsSettings)(s)
    }
  })

}
