package org.downtowndailybread


import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.AuthenticationFailedRejection.CredentialsMissing
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{RejectionHandler, _}
import akka.stream.ActorMaterializer
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import org.downtowndailybread.auth.Authentication
import org.downtowndailybread.controller.Routes
import org.downtowndailybread.exceptions.{DDBException, MalformedJsonErrorException, NotFoundException, UnauthorizedException}
import org.downtowndailybread.json.JsonSupport
import org.downtowndailybread.request.DatabaseSource
import org.flywaydb.core.Flyway

import scala.io.StdIn

object ApiMain extends JsonSupport with Routes with ApiGlobalResources {
  def main(args: Array[String]) {

    val flyway = Flyway.configure.dataSource(DatabaseSource.ds).load()
    flyway.migrate()



    implicit val system = ActorSystem("ddb-api")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    implicit def exceptionHandler: ExceptionHandler =
      ExceptionHandler {
        case r: NotFoundException =>
          extractUri {
            uri => cors() { complete((NotFound, ddbExceptionFormat.write(r)))}
          }
        case r: DDBException =>
          extractUri {
            uri => cors() {complete((BadRequest, ddbExceptionFormat.write(r)))}
          }
      }


    implicit def rejectionHandler  = RejectionHandler.newBuilder.handle {
      case MalformedRequestContentRejection(msg, throwable) ⇒ {
        val rejectionMessage = "The request content was malformed:\n" + msg
        cors() { complete(BadRequest, throw new MalformedJsonErrorException(rejectionMessage)) }
      }
      case AuthenticationFailedRejection(msg, throwable) => {
        cors() { complete(Unauthorized, new UnauthorizedException())}
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
          authenticateOAuth2("ddb-api", Authentication.authenticate) {
            user => {
              println(user)
              routes
            }
          }

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