package org.downtowndailybread.bethsaida

import java.io.{BufferedInputStream, File, FileInputStream}
import java.nio.file.{Files, Paths}

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import org.downtowndailybread.bethsaida.controller.ApplicationRoutes
import org.downtowndailybread.bethsaida.json._
import org.downtowndailybread.bethsaida.model.AnonymousUser
import org.downtowndailybread.bethsaida.providers._
import org.downtowndailybread.bethsaida.service.{ExceptionHandlers, RejectionHandlers}
import org.downtowndailybread.bethsaida.worker.EventScheduler
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.model.{ListBucketsRequest, ListObjectsRequest, PutObjectRequest}
import software.amazon.awssdk.services.s3.{S3Client, S3ClientBuilder}



object ApiMain {
  def main(args: Array[String]): Unit = {

    val settings = new Settings(args)

    val server = new ApiMain(settings)

    server.run()
  }
}

class ApiMain(val settings: Settings)
  extends JsonSupport
    with ApplicationRoutes
    with AuthenticationProvider
    with SettingsProvider
    with DatabaseConnectionProvider
    with MaterializerProvider {

  val anonymousUser = AnonymousUser

  val routes = {
    cors(CorsSettings(settings.config)) {
      pathPrefix(settings.prefix / settings.version) {
        path("") {
          complete(s"ddb api ${settings.version}")
        } ~
          allRoutes
      }
    }
  }

  implicit def exceptionHandler: ExceptionHandler = ExceptionHandlers.exceptionHandlers

  implicit def rejectionHandler: RejectionHandler = RejectionHandler.newBuilder.handle(RejectionHandlers.rejectionHanders).result

  implicit val system = ActorSystem("bethsaida-api")
  implicit val actorMaterializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  def run() = {

    val workerSystem = ActorSystem("worker-api")
    workerSystem.actorOf(Props(classOf[EventScheduler], settings), "event-scheduler")


    val bindingFuture = Http().bindAndHandle(Route.handlerFlow(routes), settings.interface, settings.port)

    println(s"Server online at http://${settings.interface}:${settings.port}/")
  }
}
