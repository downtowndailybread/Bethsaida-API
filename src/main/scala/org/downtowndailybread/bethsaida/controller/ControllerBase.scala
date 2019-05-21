package org.downtowndailybread.bethsaida.controller

import java.util.UUID

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, StandardRoute}
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.SettingsProvider
import spray.json.{JsObject, JsString}

import scala.concurrent.Future


trait ControllerBase extends SettingsProvider {
  this: JsonSupport =>

  def futureComplete(m: => ToResponseMarshallable): Route =
    extractRequestContext{
      rc =>
        implicit val ec = rc.executionContext
        StandardRoute(r => r.complete(Future(m)))
    }

  def futureCompleteCreated(m: => UUID): Route =
    extractRequestContext {
      rc =>
        implicit val ec = rc.executionContext
        complete((StatusCodes.Created, Future(m).map {
          case id => JsObject(("id", JsString(id)))
        }))
    }
}
