package org.downtowndailybread.bethsaida


import java.time.LocalDateTime

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import org.downtowndailybread.bethsaida.controller.ApplicationRoutes
import org.downtowndailybread.bethsaida.emailer.Emailer
import org.downtowndailybread.bethsaida.json._
import org.downtowndailybread.bethsaida.model.AnonymousUser
import org.downtowndailybread.bethsaida.providers._
import org.downtowndailybread.bethsaida.service.{ExceptionHandlers, RejectionHandlers}
import org.downtowndailybread.bethsaida.worker.ImageCleanup
import java.io.PrintStream
import java.time.format.DateTimeFormatter

import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.directives.{DebuggingDirectives, LogEntry}
import org.slf4j.LoggerFactory


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
    with MaterializerProvider
    with S3Provider {

  val anonymousUser = AnonymousUser

  def requestMethodAndResponseStatusAsInfo(req: HttpRequest): RouteResult => Option[LogEntry] = {
    case RouteResult.Complete(res) => Some(LogEntry(req.method.name + " " + req.uri + ": " + res.status, Logging.InfoLevel))
    case _                         => None // no log entries for rejections
  }
  val logRoutes = DebuggingDirectives.logRequestResult(requestMethodAndResponseStatusAsInfo _)

  object RouteLogger extends LoggingAdapter {
    val logger = LoggerFactory.getLogger(this.getClass)

    override def isErrorEnabled: Boolean = logger.isErrorEnabled

    override def isWarningEnabled: Boolean = logger.isWarnEnabled

    override def isInfoEnabled: Boolean = logger.isInfoEnabled

    override def isDebugEnabled: Boolean = logger.isDebugEnabled

    override protected def notifyError(message: String): Unit = logger.error(message)

    override protected def notifyError(cause: Throwable, message: String): Unit = logger.error(message, cause)

    override protected def notifyWarning(message: String): Unit = logger.warn(message)

    override protected def notifyInfo(message: String): Unit = logger.info(message)

    override protected def notifyDebug(message: String): Unit = logger.debug(message)
  }

  implicit def routeLogger: RoutingLog = RoutingLog(RouteLogger)

  val routes = {
    logRoutes {
      cors(CorsSettings(settings.config)) {
        pathPrefix(settings.prefix / settings.version) {
          path("") {
            complete(s"ddb api ${settings.version}")
          } ~
            allRoutes
        }
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

    workerSystem.actorOf(Props(classOf[ImageCleanup], settings), "image-cleanup")

    Http().bindAndHandle(Route.handlerFlow(routes), settings.interface, settings.port)

    LoggerFactory.getLogger(this.getClass).info(s"Server online at http://${settings.interface}:${settings.port}/")
  }
}
