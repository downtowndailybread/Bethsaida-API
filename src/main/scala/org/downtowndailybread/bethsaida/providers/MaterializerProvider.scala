package org.downtowndailybread.bethsaida.providers

import akka.stream.ActorMaterializer

trait MaterializerProvider {

  implicit val actorMaterializer: ActorMaterializer
}
