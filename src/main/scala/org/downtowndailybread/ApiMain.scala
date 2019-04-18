package org.downtowndailybread

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.server.ExceptionHandler
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import org.downtowndailybread.controller.Routes
import org.downtowndailybread.model.{Client, ClientAttribute, ClientAttributeType}
import org.downtowndailybread.exceptions.DDBException
import org.downtowndailybread.json.JsonSupport
import org.downtowndailybread.request.{ClientAttributeTypeRequest, ClientRequest, DatabaseSource}

import scala.io.StdIn

object ApiMain extends JsonSupport with Routes{
  def main(args: Array[String]) {

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


//    ClientAttributeTypeRequest.updateClientAttributeType(ClientAttributeType())


    val bindingFuture = Http().bindAndHandle(route, "localhost", port)
//
//
//
//    val uuid = ClientRequest.upsertClient(Client(None, Seq(ClientAttribute(ClientAttributeType("name", "string", true), "Teddy Guenin"))))
//    val client = ClientRequest.getClientById(uuid)
//    println(client)
//    val updatedClient = client.copy(client.id, ClientAttribute(ClientAttributeType("dateofbirth", "date", true), "1990-01-01") :: Nil)
//    println(ClientRequest.upsertClient(updatedClient))
//    println(ClientRequest.getClientById(uuid))

    println(s"Server online at http://localhost:$port/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}