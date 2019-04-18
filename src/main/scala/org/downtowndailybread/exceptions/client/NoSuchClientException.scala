package org.downtowndailybread.exceptions.client

import java.util.UUID

import org.downtowndailybread.exceptions.{DDBException, NoSuchIdException}

class NoSuchClientException(id: String) extends NoSuchIdException("client", id) {
  def this(uuid: UUID) {
    this(uuid.toString)
  }
}

class NoSuchClientsException(ids: Seq[UUID])
  extends DDBException(s"could not find clients with ids: ${ids.mkString("(", ", ", ")")}")
