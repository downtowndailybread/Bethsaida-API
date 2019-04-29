package org.downtowndailybread.bethsaida.service

import scala.concurrent.ExecutionContext

trait ExecutionContextProvider {
  implicit val ec: ExecutionContext
}
