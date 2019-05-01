package org.downtowndailybread.bethsaida.controller

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, StandardRoute}

import scala.concurrent.Future


object Directives {

  def futureComplete(m: => ToResponseMarshallable): Route =
    extractRequestContext{
      rc =>
        StandardRoute(r => r.complete(Future(m)(rc.executionContext)))
    }
}
