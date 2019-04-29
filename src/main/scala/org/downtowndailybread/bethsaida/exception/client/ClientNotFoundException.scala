package org.downtowndailybread.bethsaida.exception.client

import java.util.UUID

import org.downtowndailybread.bethsaida.exception.NoSuchIdException

class ClientNotFoundException(id: String) extends NoSuchIdException("client", id) {
  def this(uuid: UUID) {
    this(uuid.toString)
  }
}
