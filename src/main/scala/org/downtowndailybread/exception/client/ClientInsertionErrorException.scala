package org.downtowndailybread.exception.client

import java.util.UUID

import org.downtowndailybread.exception.NoSuchIdException

class ClientInsertionErrorException(id: String) extends NoSuchIdException("client", id) {
  def this(uuid: UUID) {
    this(uuid.toString)
  }
}
