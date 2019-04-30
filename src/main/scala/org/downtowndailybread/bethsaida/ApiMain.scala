package org.downtowndailybread.bethsaida

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes.{BadRequest, NotFound, Unauthorized}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import com.typesafe.config.Config
import org.downtowndailybread.bethsaida.controller.ApplicationRoutes
import org.downtowndailybread.bethsaida.exception._
import org.downtowndailybread.bethsaida.json._
import org.downtowndailybread.bethsaida.service.SecretProvider

import scala.concurrent.Promise
import scala.io.StdIn

object ApiMain extends JsonSupport with SecretProvider with ApplicationRoutes with ApiGlobalResources {

  val secret = "changeme"
  val config = Promise[Config]

  def main(args: Array[String]) {
    implicit val system = ActorSystem("ddb-api")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    implicit def exceptionHandler: ExceptionHandler =
      ExceptionHandler {
        case r: NotFoundException =>
          extractUri {
            uri => cors() {
              complete((NotFound, ddbExceptionFormat.write(r)))
            }
          }
        case r: UnauthorizedException =>
          extractUri {
            uri => cors() {
              complete((Unauthorized, ddbExceptionFormat.write(r)))
            }
          }
        case r: DDBException =>
          extractUri {
            uri => cors() {
              complete((BadRequest, ddbExceptionFormat.write(r)))
            }
          }

      }


    implicit def rejectionHandler = RejectionHandler.newBuilder.handle {
      case MalformedRequestContentRejection(msg, _) ⇒ {
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

    val route = cors() {
      path("swagger") {
        getFromResource("swagger/index.html")
      } ~
        getFromResourceDirectory("swagger") ~
        pathPrefix(apiPathPrefix) {
          path("") {
            complete(s"ddb api $version")
          } ~
            allRoutes
        }
    }

    val bindingFuture = Http().bindAndHandle(route, "localhost", port)

    println(s"Server online at http://localhost:$port/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
