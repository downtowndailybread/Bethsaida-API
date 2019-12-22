package org.downtowndailybread.bethsaida

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import com.typesafe.config.ConfigFactory
import org.downtowndailybread.bethsaida.controller.ApplicationRoutes
import org.downtowndailybread.bethsaida.json._
import org.downtowndailybread.bethsaida.model.AnonymousUser
import org.downtowndailybread.bethsaida.providers._
import org.downtowndailybread.bethsaida.service.{ExceptionHandlers, RejectionHandlers}
import org.downtowndailybread.bethsaida.worker.EventScheduler

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
    with SettingsProvider
    with DatabaseConnectionProvider {

  val anonymousUser = AnonymousUser

  val routes = {
    cors() {
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

  def run() = {
    implicit val system = ActorSystem("bethsaida-api")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val workerSystem = ActorSystem("worker-api")
    workerSystem.actorOf(Props(classOf[EventScheduler], settings), "event-scheduler")


    val bindingFuture = Http().bindAndHandle(Route.handlerFlow(routes), settings.interface, settings.port)

    println(s"Server online at http://${settings.interface}:${settings.port}/")
  }
}
