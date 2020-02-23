package org.downtowndailybread.integration.base

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.typesafe.config.ConfigFactory
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.parameters.{LoginParameters, UserParameters}
import org.downtowndailybread.bethsaida.providers.{DatabaseConnectionProvider, SettingsProvider}
import org.downtowndailybread.bethsaida.{ApiMain, Settings}
import org.scalatest.FlatSpec
import spray.json.JsString

import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}

trait BethsaidaSupport
  extends FlatSpec
    with ScalatestRouteTest
    with JsonSupport
    with SettingsProvider
    with DatabaseConnectionProvider {
  lazy val settings = new Settings(Array("integration_test"))

  lazy val apiMain = new ApiMain(settings)

  lazy val apiBaseUrl = "/api/v1"

  implicit val d = apiMain.exceptionHandler
  implicit val r = apiMain.rejectionHandler

  lazy val routes = Route.seal(apiMain.routes)

  protected val authTokenPromise = Promise[String]()

  lazy val authToken = Await.result(authTokenPromise.future, 50.milli)

  lazy val authHeader = new Authorization(OAuth2BearerToken(authToken))

  lazy val userParams = UserParameters(
    "Andy Guenin",
    LoginParameters(
      "andy@guenin.com",
      "AndyGueninPassword"
    )
  )

  class EnhancedHttpRequest(val httpRequest: HttpRequest) {
    def authenticate(): HttpRequest = httpRequest.withHeaders(authHeader)
  }

  implicit def httpRequestToEnhanced(httpRequest: HttpRequest): EnhancedHttpRequest = {
    new EnhancedHttpRequest(httpRequest)
  }

  implicit def stringToJsString(s: String): JsString = JsString(s)
}
