package org.downtowndailybread.exceptions.client

import java.util.UUID

import org.downtowndailybread.exceptions.NoSuchIdException

class ClientNotFoundException(id: String) extends NoSuchIdException("client", id) {
  def this(uuid: UUID) {
    this(uuid.toString)
  }
}
