package org.downtowndailybread


import akka.actor.ActorSystem
import akka.http.scaladsl.server.ExceptionHandler
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import org.downtowndailybread.controller.Routes
import org.downtowndailybread.exceptions.DDBException
import org.downtowndailybread.json.JsonSupport
import org.downtowndailybread.request.DatabaseSource
import org.flywaydb.core.Flyway


import scala.io.StdIn

object ApiMain extends JsonSupport with Routes{
  def main(args: Array[String]) {

    val flyway = Flyway.configure.dataSource(DatabaseSource.ds).load()
    flyway.migrate()


    implicit val system = ActorSystem("ddb-api")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val port = 8090

    implicit def exceptionHandler: ExceptionHandler =
      ExceptionHandler {
        case r : DDBException =>
          extractUri {
            uri => complete(ddbExceptionFormat.write(r))
          }
      }

    val route = ignoreTrailingSlash {
    pathPrefix("api" / "v1") {
      path("") {
        complete("ddb api v1")
      } ~
        routes
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