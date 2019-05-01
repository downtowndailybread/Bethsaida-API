package org.downtowndailybread.bethsaida

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes.{BadRequest, NotFound, Unauthorized}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import com.typesafe.config.ConfigFactory
import org.downtowndailybread.bethsaida.controller.ApplicationRoutes
import org.downtowndailybread.bethsaida.exception._
import org.downtowndailybread.bethsaida.json._
import org.downtowndailybread.bethsaida.request.{DatabaseSource, UserRequest}
import org.downtowndailybread.bethsaida.service._

import scala.io.StdIn

object ApiMain {
  def main(args: Array[String]): Unit = {
    val settings = new Settings(ConfigFactory.load())

    val server = new ApiMain(settings)

    server.run()
  }
}

class ApiMain(val settings: Settings)
  extends JsonSupport
    with ApplicationRoutes
    with AuthenticationProvider
    with SettingsProvider {

  val anonymousUser = DatabaseSource.runSql(conn => new UserRequest(conn).getAnonymousUser())

  def run() = {
    implicit val system = ActorSystem("ddb-api")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    implicit def exceptionHandler: ExceptionHandler =
      ExceptionHandler {
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

    implicit def rejectionHandler = RejectionHandler.newBuilder.handle {
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
    }.result

    val routes = cors() {
      path("swagger") {
        getFromResource("swagger/index.html")
      } ~
        getFromResourceDirectory("swagger") ~
        pathPrefix(settings.prefix / settings.version) {
          path("") {
            complete(s"ddb api ${settings.version}")
          } ~
            allRoutes
        }
    }

    val bindingFuture = Http().bindAndHandle(Route.handlerFlow(routes), "localhost", settings.port)

    println(s"Server online at http://localhost:${settings.port}/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
